package gatewayUtils

import exceptions.InvalidArgumentException
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.Answer
import utils.AnswerType
import utils.Query
import utils.QueryType
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.LinkedList
import java.util.concurrent.ForkJoinPool

/**
 * Class responsible for managing network connections
 */
class ConnectionManager {
    // don't represent anything, used only for initializing the vars
    private var host = "localhost"
    private var portForClient = 0
    private var portForServer = 0
    private var portForPinging = 0

    private val logger: Logger = LogManager.getLogger(ConnectionManager::class.java)
    private val pool = ForkJoinPool.commonPool()

    var addressForClient = InetSocketAddress(host, portForClient)
    var addressForServer = InetSocketAddress(host, portForServer)
    var addressForPinging = InetSocketAddress(host, portForPinging)

    var datagramChannelClient: DatagramChannel = DatagramChannel.open()
    var datagramChannelServer: DatagramChannel = DatagramChannel.open()
    private var buffer = ByteBuffer.allocate(8192)

    var remoteAddressClient = InetSocketAddress(portForClient)
    var remoteAddressServer = InetSocketAddress(portForServer)
    val availableServers = LinkedList<InetSocketAddress>()
    //val serversOnCheck = mutableMapOf<InetSocketAddress, Timestamp>()

    private val timeout = 15000
    private var datagramSocket = DatagramSocket()
    private var datagramPacket = DatagramPacket(ByteArray(8192), 8192, InetAddress.getByName(host), portForPinging)

    /**
     * Starts the server at given host and port
     */
    fun startGateway(host: String, portClient: Int, portServer: Int, portPing: Int) {
        this.host = host
        this.portForClient = portClient
        this.portForServer = portServer
        this.portForPinging = portPing
        var unboundClient = true
        var unboundServer = true
        var unboundPing = true
        while (unboundClient and unboundServer and unboundPing) {
            try {
                this.portForClient++
                this.addressForClient = InetSocketAddress(this.host, portForClient)
                datagramChannelClient.bind(addressForClient)
                unboundClient = false
                this.portForServer++
                this.addressForServer = InetSocketAddress(this.host, portForServer)
                datagramChannelServer.bind(addressForServer)
                unboundServer = false

                this.datagramSocket = DatagramSocket(portForPinging)
                this.datagramSocket.soTimeout = timeout
                unboundPing = false

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
                "serverSocket" -> {
                    val dat = data.array()
                    datagramPacket = DatagramPacket(dat, dat.size, address)
                    datagramSocket.send(datagramPacket)
                }
                "serverChannel" -> {
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
        buffer = ByteBuffer.allocate(8192)
        remoteAddressClient = datagramChannelClient.receive(buffer) as InetSocketAddress
        val jsonQuery = buffer.array().decodeToString().replace("\u0000", "")
        logger.info("Received: $jsonQuery")
        return Json.decodeFromString(Query.serializer(), jsonQuery)
    }
    /**
     * Reads and decodes the incoming answer
     * @return Answer object
     */
    private fun receiveFromServerSocket() : Answer {
        val data = ByteArray(8192)
        val jsonAnswer : String
        datagramPacket = DatagramPacket(data, data.size)
        try {
            datagramSocket.receive(datagramPacket)
            jsonAnswer = data.decodeToString().replace("\u0000", "")
        } catch (e:Exception) {
            datagramPacket = DatagramPacket(ByteArray(8192), 8192, InetAddress.getByName(host), 0)
            return Answer(AnswerType.ERROR, e.message.toString(), receiver = "")
        }
        logger.info("Received: $jsonAnswer")
        return Json.decodeFromString(Answer.serializer(), jsonAnswer)
    }

    fun receiveFromServer() : Answer {
        buffer = ByteBuffer.allocate(8192)
        remoteAddressServer = datagramChannelServer.receive(buffer) as InetSocketAddress
        val jsonAnswer = buffer.array().decodeToString().replace("\u0000", "")
        logger.info("Received: $jsonAnswer")
        return Json.decodeFromString(Answer.serializer(), jsonAnswer)
    }

    /**
     * Encodes and sends the answer to the client
     */
    fun sendToClient(answer: Answer, address: InetSocketAddress) {
        buffer = ByteBuffer.allocate(8192)
        logger.info("Sending answer to {}", address)
        logger.info("Sending: ${Json.encodeToString(Answer.serializer(), answer)}")
        val jsonAnswer = Json.encodeToString(Answer.serializer(), answer).toByteArray()
        val data = ByteBuffer.wrap(jsonAnswer)
        sendAsync(data, address, "client")
    }

    /**
     * Encodes and sends the answer to the server
     */
    fun sendToServer(query: Query, address: InetSocketAddress, channelOrSocket: String) {
        buffer = ByteBuffer.allocate(8192)
        logger.info("Sending query to {}", address)
        val jsonQuery = Json.encodeToString(Query.serializer(), query).toByteArray()
        logger.info("Sending: ${Json.encodeToString(Query.serializer(), query)}")
        val data = ByteBuffer.wrap(jsonQuery)
        if (channelOrSocket == "socket") {
            sendAsync(data, address, "serverSocket")
        } else if (channelOrSocket == "channel") {
            sendAsync(data, address, "serverChannel")
        }

        //serversOnCheck[address] = Timestamp(System.currentTimeMillis())
    }

    fun connected(address: InetSocketAddress): Boolean {
        return ping(address) < timeout
    }

    private fun ping(address: InetSocketAddress) : Double {
        val query = Query(QueryType.PING, "Ping", mutableMapOf("sender" to addressForServer.toString()))
        try {
            sendToServer(query, address, "socket")
        } catch (e:Exception) {
            return timeout.toDouble()
        }

        val startTime = System.nanoTime()
        receiveFromServerSocket()
        val elapsedTimeInMs = (System.nanoTime() - startTime).toDouble() / 1000000
        logger.info("Ping with server: $elapsedTimeInMs ms")
        return elapsedTimeInMs
    }
}
