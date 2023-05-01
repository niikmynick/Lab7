package gatewayUtils

import exceptions.InvalidArgumentException
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.Answer
import utils.Query
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.LinkedList
import java.util.concurrent.ForkJoinPool

/**
 * Class responsible for managing network connections
 */
class ConnectionManager {
    private var host = "localhost"
    private var portForClient = 6789
    private var portForServer = 6790

    private val logger: Logger = LogManager.getLogger(ConnectionManager::class.java)
    private val pool = ForkJoinPool.commonPool()

    var addressForClient = InetSocketAddress(host, portForClient)
    var addressForServer = InetSocketAddress(host, portForServer)

    var datagramChannelClient: DatagramChannel = DatagramChannel.open()
    var datagramChannelServer: DatagramChannel = DatagramChannel.open()
    private var buffer = ByteBuffer.allocate(4096)

    private var remoteAddressClient = InetSocketAddress(portForClient)
    private var remoteAddressServer = InetSocketAddress(portForServer)

    val availableServers = LinkedList<InetSocketAddress>()

    /**
     * Starts the server at given host and port
     */
    fun startGateway(host: String, portClient: Int, portServer: Int) {
        this.host = host
        this.portForClient = portClient
        this.portForServer = portServer
        var unboundClient = true
        var unboundServer = true
        while (unboundClient and unboundServer) {
            try {
                this.portForClient++
                this.addressForClient = InetSocketAddress(host, portForClient)
                datagramChannelClient.bind(addressForClient)
                unboundClient = false
                this.portForServer++
                this.addressForServer = InetSocketAddress(host, portForServer)
                datagramChannelServer.bind(addressForServer)
                unboundServer = false
            } catch (_:Exception) {}
        }

        datagramChannelClient.configureBlocking(false)
        datagramChannelServer.configureBlocking(false)
        logger.info("Gateway started on $addressForClient for Clients")
        logger.info("Gateway started on $addressForServer for Servers")
    }

    private fun sendAsync(data: ByteBuffer, address: InetSocketAddress, string: String) {
        pool.execute {
            when (string) {
                "server" -> {
                    datagramChannelServer.send(data, address)
                }
                "client" -> {
                    datagramChannelClient.send(data, address)
                }
                else -> {
                    throw InvalidArgumentException("Invalid receiver type. Should be server or client")
                }
            }
        }
    }

    /**
     * Reads and decodes the incoming query
     * @return Query object
     */
    fun receiveFromClient() : Query{
        buffer = ByteBuffer.allocate(4096)
        remoteAddressClient = datagramChannelClient.receive(buffer) as InetSocketAddress
        val jsonQuery = buffer.array().decodeToString().replace("\u0000", "")
        logger.info("Received: $jsonQuery")
        return Json.decodeFromString(Query.serializer(), jsonQuery)
    }
    /**
     * Reads and decodes the incoming answer
     * @return Answer object
     */
    fun receiveFromServer() : Answer{
        buffer = ByteBuffer.allocate(4096)
        remoteAddressServer = datagramChannelServer.receive(buffer) as InetSocketAddress
        val jsonAnswer = buffer.array().decodeToString().replace("\u0000", "")
        logger.info("Received: $jsonAnswer")
        return Json.decodeFromString(Answer.serializer(), jsonAnswer)
    }

    /**
     * Encodes and sends the answer to the client
     */
    fun sendToClient(answer: Answer) {
        buffer = ByteBuffer.allocate(4096)
        logger.info("Sending answer to {}", remoteAddressClient)
        logger.info("Sending: ${Json.encodeToString(Answer.serializer(), answer)}")
        val jsonAnswer = Json.encodeToString(Answer.serializer(), answer).toByteArray()
        val data = ByteBuffer.wrap(jsonAnswer)
        sendAsync(data, remoteAddressClient, "client")
    }

    /**
     * Encodes and sends the answer to the server
     */
    fun sendToServer(query: Query) {
        buffer = ByteBuffer.allocate(4096)
        logger.info("Sending answer to {}", remoteAddressServer)
        val jsonQuery = Json.encodeToString(Query.serializer(), query).toByteArray()
        logger.info("Sending: ${Json.encodeToString(Query.serializer(), query)}")
        val data = ByteBuffer.wrap(jsonQuery)
        sendAsync(data, remoteAddressServer, "server")
    }
}
