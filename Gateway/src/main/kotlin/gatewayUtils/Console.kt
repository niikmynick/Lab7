package gatewayUtils


import multithread.FromClientThread
import multithread.FromServerThread
import utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
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
    //private val threadPool = ThreadPoolExecutor(0, 10, 0L, java.util.concurrent.TimeUnit.MILLISECONDS, java.util.concurrent.LinkedBlockingQueue())
    private val fromServerThreadPool = Executors.newCachedThreadPool()
    private val fromClientThreadPool = Executors.newCachedThreadPool()
    private val answerQueue = LinkedBlockingQueue<Answer>()
    private val queryQueue = LinkedBlockingQueue<Query>()

    private val serverThread = FromServerThread(connectionManager, answerQueue)
    private val clientThread = FromClientThread(connectionManager, queryQueue)
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
                    val activeChannel = key.channel() as DatagramChannel
                    when (activeChannel.localAddress) {
                        connectionManager.addressForServer -> {
                            connectionManager.datagramChannelServer = activeChannel
                            val received = connectionManager.receiveFromServer()
                            answerQueue.put(received)

                            fromServerThreadPool.execute {
                                serverThread.run()
                            }

                        }
                        connectionManager.addressForClient -> {
                            connectionManager.datagramChannelClient = activeChannel

                            val received = connectionManager.receiveFromClient()
                            val clientAddress = connectionManager.remoteAddressClient
                            received.args["sender"] = clientAddress.toString()
                            queryQueue.put(received)

                            fromClientThreadPool.execute {
                                clientThread.run()
                            }

                        }
                        else -> {}
                    }

                }
            }
        }

        fromClientThreadPool.shutdown()
        fromServerThreadPool.shutdown()
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
