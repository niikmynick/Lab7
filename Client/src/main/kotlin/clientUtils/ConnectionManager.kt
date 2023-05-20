package clientUtils

import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * The ConnectionManager class is responsible for managing a connection with a server using UDP protocol.
 */
class ConnectionManager(host: String, private var port: Int) {
    /**
     * The timeout for connection in milliseconds
     */
    private val timeout = 15000
    private var datagramSocket = DatagramSocket()
    private val outputManager = OutputManager()
    private var hostInetAddress = InetAddress.getByName(host)
    private var datagramPacket = DatagramPacket(ByteArray(8192), 8192, hostInetAddress, port)

    private val logger: Logger = LogManager.getLogger(ConnectionManager::class.java)
    init {
        var unbound = true
        var port = 6789
        while (unbound) {
            try {
                port++
                datagramSocket = DatagramSocket(port)
                unbound = false
                logger.debug("Bound on port: $port")
            } catch (e:Exception) {
                logger.warn("Found closed port" + e.message)
            }
        }

    }

    /**
     * Checks if the ConnectionManager is connected to the server by sending a ping packet
     * and measuring the response time.
     * @return true if the connection is established (ping is less than timeout), false otherwise.
     */
    fun connected(): Boolean {
        datagramSocket.soTimeout = timeout
        return ping() < timeout
    }

    /**
     * Sends a ping Query to the server and waits for a response.
     * If an error occurs during sending, an Answer with the error message is returned.
     * @return Time elapsed from sending the query to receiving the answer
     */
    private fun ping() : Double {
        val query = Query(QueryType.PING, "Ping", mutableMapOf())
        try {
            send(query)
        } catch (e:Exception) {
            outputManager.println(e.message.toString())
            return timeout.toDouble()
        }

        val startTime = System.nanoTime()
        receive()
        val elapsedTimeInMs = (System.nanoTime() - startTime).toDouble() / 1000000
        logger.info("Ping with server: $elapsedTimeInMs ms")
        return elapsedTimeInMs
    }

    /**
     * Sends a Query to the server and waits for a response.
     * If an error occurs during sending, an Answer with the error message is returned.
     * @param query The Query to send to the server.
     * @return The Answer received from the server, or an Answer with an error message if an error occurred.
     */
    fun checkedSendReceive(query: Query) : Answer {
        try {
            send(query)
        } catch (e:Exception) {
            return Answer(AnswerType.ERROR, e.message.toString(), receiver = "")
        }
        return receive()
    }

    /**
     * Sends a Query to the server.
     * @param query The Query to send to the server.
     */
    fun send(query: Query) {
        val jsonQuery = Json.encodeToString(Query.serializer(), query)
        val data = jsonQuery.toByteArray()
        hostInetAddress = datagramPacket.address
        port = datagramPacket.port
        logger.info("Sending: $jsonQuery to $hostInetAddress:$port")
        datagramPacket = DatagramPacket(data, data.size, hostInetAddress, port)
        datagramSocket.send(datagramPacket)
    }

    /**
     * This function receives an Answer from the server by reading the incoming data from the datagramSocket.
     * It creates a datagramPacket to store the incoming data and read it into the packet.
     * If there is an exception during the read, it returns an Answer object with type ERROR and the exception message.
     * Otherwise, it decodes the incoming data into a JSON string and then deserializes it into an Answer object.
     * @return Answer: The Answer object received from the server.
     */
    private fun receive(): Answer {
        val data = ByteArray(8192)
        val jsonAnswer : String
        datagramPacket = DatagramPacket(data, data.size)
        try {
            datagramSocket.receive(datagramPacket)
            jsonAnswer = data.decodeToString().replace("\u0000", "")
        } catch (e:Exception) {
            datagramPacket = DatagramPacket(ByteArray(8192), 8192, hostInetAddress, port)
            return Answer(AnswerType.ERROR, e.message.toString(), receiver = "")
        }

        logger.info("Received: $jsonAnswer")
        return Json.decodeFromString(Answer.serializer(), jsonAnswer)
    }

}