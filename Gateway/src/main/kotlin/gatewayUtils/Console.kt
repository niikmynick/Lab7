package gatewayUtils


import utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import kotlin.concurrent.timerTask

/**
 * Class that handles user commands and provides them all the necessary parameters
 * @property connectionManager Manages connections to the server
 * @property fileManager Used for loading data to the collection
 * @property collectionManager Manages the collection of objects
 * @property commandInvoker Invokes commands that operate on the collection
 * @property commandReceiver Receives commands and executes them
 */
class Console {
    // connection
    private val connectionManager = ConnectionManager()
    private val selector = Selector.open()

    // utils
    private val jsonCreator = JsonCreator()
    private val logger: Logger = LogManager.getLogger(Console::class.java)
    private var executeFlag = true

    // auto save
    private val timer = Timer()

    // multithreading
    private val threadPool = ThreadPoolExecutor(0, 10, 0L, java.util.concurrent.TimeUnit.MILLISECONDS, java.util.concurrent.LinkedBlockingQueue())

    fun start(actions: ConnectionManager.() -> Unit) {
        connectionManager.actions()
    }

    /**
     * Registers commands and waits for user prompt
     */
    fun initialize() {

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
        connectionManager.datagramChannel.register(selector, SelectionKey.OP_READ)

        while (executeFlag) {
            selector.select()
            val selectedKeys = selector.selectedKeys()

            if (selectedKeys.isEmpty()) continue

            val iter = selectedKeys.iterator()

            while (iter.hasNext()) {
                val key = iter.next()
                iter.remove()
                if (key.isReadable) {
                    val client = key.channel() as DatagramChannel
                    try {
                        connectionManager.datagramChannel = client
                        val query = connectionManager.receive()
                        threadPool.execute {

                        }
                    } catch (e: Exception) {
                        logger.error("Error while executing command: ${e.message}")
                        val answer = Answer(AnswerType.ERROR, e.message.toString())
                        connectionManager.send(answer)
                    }
                }
            }
        }

        threadPool.shutdown()

        connectionManager.datagramChannel.close()
        selector.close()
    }

    fun stop() {
        logger.info("Closing gateway")

        executeFlag = false

        selector.wakeup()
        timer.cancel()
    }

}
