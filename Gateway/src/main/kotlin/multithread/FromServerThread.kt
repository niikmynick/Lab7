package multithread

import gatewayUtils.ConnectionManager
import utils.Answer
import utils.AnswerType
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue

class FromServerThread(private val connectionManager: ConnectionManager, private val answerQueue: LinkedBlockingQueue<Answer>) : Runnable {

    override fun run() {
        val serverAddress = connectionManager.remoteAddressServer
        val received = answerQueue.take() as Answer
        if (received.answerType == AnswerType.REGISTRATION_REQUEST) {
            if (received.message == "Closing Server") {
                connectionManager.availableServers.remove(serverAddress)
            } else connectionManager.availableServers.add(serverAddress)
        } else {
            val receiver = received.receiver.split(':')
            val address = InetSocketAddress(receiver[0].replace("/",""), receiver[1].toInt())
            if ((address != connectionManager.addressForPinging) and (address != connectionManager.addressForServer)) {
                connectionManager.sendToClient(received, address)
            }
        }
    }
}