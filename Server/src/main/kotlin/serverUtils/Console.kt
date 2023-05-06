package serverUtils

import collection.CollectionManager
import commands.CommandInvoker
import commands.CommandReceiver
import commands.consoleCommands.*
import multithread.ReceiverThread
import multithread.SenderThread
import utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import tokenutils.JWTManager
import users.UserManager
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.*
import java.util.concurrent.*
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

    // collection
    //private val dbManager = DBManager("jdbc:postgresql://localhost:5432/studs", "s368311", "cvyPME6q769KBBWn")
    private val dbManager = DBManager("jdbc:postgresql://localhost:5432/studs", "s372819", "cfJSPKlqsJNlLcPg")
    private val fileManager = FileManager(dbManager)
    private val collectionManager = CollectionManager(dbManager)

    // users
    private val userManager = UserManager(dbManager)

    // commands
    private val commandInvoker = CommandInvoker(connectionManager)
    private val commandReceiver = CommandReceiver(collectionManager, connectionManager)

    // utils
    private val jsonCreator = JsonCreator()
    private val logger: Logger = LogManager.getLogger(Console::class.java)
    private var executeFlag = true
    private val jwtManager = JWTManager()

    // auto save
    private val timer = Timer()

    // multithreading
    //private val threadPool = ThreadPoolExecutor(0, 10, 0L, java.util.concurrent.TimeUnit.MILLISECONDS, java.util.concurrent.LinkedBlockingQueue())
    private val cachedPool = Executors.newCachedThreadPool()
    private val forkJoinPool = ForkJoinPool.commonPool()
    private val taskQueue = LinkedBlockingQueue<Sending>(10)
    private val answerQueue = LinkedBlockingQueue<Sending>(10)
    private val threadReceiver = ReceiverThread(taskQueue, fileManager, collectionManager, jwtManager, commandInvoker, userManager, jsonCreator, answerQueue)
    private val threadSender = SenderThread(answerQueue, connectionManager)
    fun start(actions: ConnectionManager.() -> Unit) {
        connectionManager.actions()
    }

    /**
     * Registers commands and waits for user prompt
     */
    fun initialize() {
        dbManager.initDB()

        logger.info("Initializing the server")

        commandInvoker.register("info", Info(commandReceiver))
        logger.debug("Command 'info' registered")

        commandInvoker.register("show", Show(commandReceiver))
        logger.debug("Command 'show' registered")

        commandInvoker.register("add", Add(commandReceiver))
        logger.debug("Command 'add' registered")

        commandInvoker.register("update_id", Update(commandReceiver))
        logger.debug("Command 'update_id' registered")

        commandInvoker.register("remove_by_id", RemoveID(commandReceiver))
        logger.debug("Command 'remove_by_id' registered")

        commandInvoker.register("clear", Clear(commandReceiver))
        logger.debug("Command 'clear' registered")

        commandInvoker.register("add_if_min", AddMin(commandReceiver))
        logger.debug("Command 'add_if_min' registered")

        commandInvoker.register("remove_greater", RemoveGreater(commandReceiver))
        logger.debug("Command 'remove_greater' registered")

        commandInvoker.register("remove_lower", RemoveLower(commandReceiver))
        logger.debug("Command 'remove_lower' registered")

        commandInvoker.register("remove_any_by_chapter", RemoveAnyChapter(commandReceiver))
        logger.debug("Command 'remove_any_by_chapter' registered")

        commandInvoker.register("count_by_melee_weapon", CountByMeleeWeapon(commandReceiver))
        logger.debug("Command 'count_by_melee_weapon' registered")

        commandInvoker.register("filter_by_chapter", FilterByChapter(commandReceiver))
        logger.debug("Command 'filter_by_chapter' registered")

        commandInvoker.register("filter_by_weapon", FilterByWeapon(commandReceiver))
        logger.debug("Command 'filter_by_weapon' registered")

        fileManager.load(collectionManager)
        logger.info("Collection loaded")

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
        }, 120000, time) //120000
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
                    connectionManager.datagramChannel = client

                    val query = connectionManager.receive()
                    taskQueue.put(query)

                    cachedPool.execute {
                        threadReceiver.run()
                    }

                    forkJoinPool.execute {
                        threadSender.run()
                    }

                }
            }
        }

        cachedPool.awaitTermination(5000, TimeUnit.MILLISECONDS)
        forkJoinPool.awaitTermination(5000, TimeUnit.MILLISECONDS)
        connectionManager.datagramChannel.close()
        selector.close()
    }

    fun stop() {
        logger.info("Closing server")

        executeFlag = false

        selector.wakeup()
        timer.cancel()
    }

    fun save() {
        try {
            fileManager.save(collectionManager, userManager)
            logger.info("Collection saved successfully")
        } catch (e:Exception) {
            logger.warn("Collection was not saved: ${e.message}")
        }
    }

}
