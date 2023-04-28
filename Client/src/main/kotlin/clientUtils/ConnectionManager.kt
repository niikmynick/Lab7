package clientUtils

import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class ConnectionManager(host: String, private var port: Int) {

    private val timeout = 5000
    private var datagramSocket = DatagramSocket()
    private val outputManager = OutputManager()
    private var hostInetAddress = InetAddress.getByName(host)
    private var datagramPacket = DatagramPacket(ByteArray(4096), 4096, hostInetAddress, port)

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
            } catch (_:Exception) {}
        }

    }

    fun connected(): Boolean {
        datagramSocket.soTimeout = timeout
        return ping() < timeout
    }

    private fun ping() : Double {
        val query = Query(QueryType.PING, "Ping", mapOf())
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

    fun checkedSendReceive(query: Query) : Answer {
        try {
            send(query)
        } catch (e:Exception) {
            return Answer(AnswerType.ERROR, e.message.toString())
        }
        return receive()
    }

    fun send(query: Query) {
        val jsonQuery = Json.encodeToString(Query.serializer(), query)
        val data = jsonQuery.toByteArray()
        hostInetAddress = datagramPacket.address
        port = datagramPacket.port
        logger.info("Sending: $jsonQuery to $hostInetAddress:$port")
        datagramPacket = DatagramPacket(data, data.size, hostInetAddress, port)
        datagramSocket.send(datagramPacket)
    }

    private fun receive(): Answer {
        val data = ByteArray(4096)
        val jsonAnswer : String
        datagramPacket = DatagramPacket(data, data.size)
        try {
            datagramSocket.receive(datagramPacket)
            jsonAnswer = data.decodeToString().replace("\u0000", "")
        } catch (e:Exception) {
            datagramPacket = DatagramPacket(ByteArray(4096), 4096, hostInetAddress, port)
            return Answer(AnswerType.ERROR, e.message.toString())
        }

        logger.info("Received: $jsonAnswer")
        return Json.decodeFromString(Answer.serializer(), jsonAnswer)
    }

}