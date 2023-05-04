package gatewayUtils



import utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import kotlin.concurrent.timerTask

/**
 * Class that handles user commands and provides them all the necessary parameters
 * @property connectionManager Manages connections to the gateway
 */
class Console {
    // connection
    private val connectionManager = ConnectionManager()
    private val selector = Selector.open()

    // utils
    private val logger: Logger = LogManager.getLogger(Console::class.java)
    private var executeFlag = true

    // auto save
    private val timer = Timer()

    // multithreading
    private val threadPool = ThreadPoolExecutor(0, 10, 0L, java.util.concurrent.TimeUnit.MILLISECONDS, java.util.concurrent.LinkedBlockingQueue())

    fun start(actions: ConnectionManager.() -> Unit) {
        connectionManager.actions()
    }


    private fun onTimeComplete(actions: Console.() -> Unit) {
        actions()
    }

    fun scheduleTask(time:Long, actions: Console.() -> Unit) {

        timer.schedule(timerTask {
            run {
                onTimeComplete {
                    actions()
                }
            }
        }, 120000, time)
    }

    /**
     * Enters interactive mode and waits for incoming queries
     */
    fun startInteractiveMode() {
        logger.info("The server is ready to receive commands")
        connectionManager.datagramChannelClient.register(selector, SelectionKey.OP_READ)
        connectionManager.datagramChannelServer.register(selector, SelectionKey.OP_READ)


        while (executeFlag) {
            selector.select()
            val selectedKeys = selector.selectedKeys()

            if (selectedKeys.isEmpty()) continue

            val iter = selectedKeys.iterator()

            while (iter.hasNext()) {
                val key = iter.next()
                iter.remove()
                if (key.isReadable) {
                    val request = key.channel() as DatagramChannel
                    when (request.localAddress) {
                        connectionManager.addressForServer -> {
                            connectionManager.datagramChannelServer = request
                            val received = connectionManager.receiveFromServer()
                            threadPool.execute {
                                logger.info("Received from server: $received")
                                val serverAddress = connectionManager.remoteAddressServer
                                if (received.answerType == AnswerType.REGISTRATION_REQUEST) {
                                    connectionManager.availableServers += serverAddress
                                } else {
                                    val host = received.receiver.split(':')[0].replace("/","")
                                    val port = received.receiver.split(':')[1].toInt()
                                    val address = InetSocketAddress(host, port)
                                    connectionManager.sendToClient(received, address)
                                }

                            }
                        }
                        connectionManager.addressForClient -> {
                            connectionManager.datagramChannelClient = request
                            val received = connectionManager.receiveFromClient()
                            threadPool.execute {
                                logger.info("Received from client: $received")
                                val clientAddress = connectionManager.remoteAddressClient
                                received.args["sender"] = clientAddress.toString()
                                try {
                                    do {
                                        val address = connectionManager.availableServers.pop()
                                        val isConnected = connectionManager.connected(address)
                                        if (isConnected) {
                                            connectionManager.availableServers.addLast(address)
                                            connectionManager.sendToServer(received, address, "channel")
                                        }
                                    } while (!isConnected)
                                } catch (e:Exception) {
                                    logger.warn("No server was found")
                                    logger.warn(e.message)
                                }


                            }
                        }
                        else -> {}
                    }

                }
            }
        }

        threadPool.shutdown()
        connectionManager.datagramChannelClient.close()
        connectionManager.datagramChannelServer.close()
        selector.close()
    }

    fun stop() {
        logger.info("Closing gateway")

        executeFlag = false

        selector.wakeup()
        timer.cancel()
    }

}
