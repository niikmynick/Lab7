package serverUtils

import collection.CollectionManager
import commands.CommandInvoker
import commands.CommandReceiver
import commands.consoleCommands.*
import utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import users.UserManager
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.ForkJoinPool
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

    // collection
    //private val dbManager = DBManager("jdbc:postgresql://localhost:5432/studs", "s368311", "cvyPME6q769KBBWn")
    private val dbManager = DBManager("jdbc:postgresql://localhost:5432/studs", "s372819", "")
    private val fileManager = FileManager(dbManager)
    private val collectionManager = CollectionManager()

    // users
    private val userManager = UserManager(dbManager)

    // commands
    private val commandInvoker = CommandInvoker(connectionManager)
    private val commandReceiver = CommandReceiver(collectionManager, connectionManager)

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
                            when (query.queryType) {

                                QueryType.COMMAND_EXEC -> {
                                    logger.info("Received command: ${query.information}")
                                    if (query.token in userManager.getTokens()) {
                                        commandInvoker.executeCommand(query)

                                    } else {
                                        val answer = Answer(AnswerType.ERROR, "Unknown token. Authorize again.")
                                        connectionManager.send(answer)
                                    }
                                }

                                QueryType.INITIALIZATION -> {
                                    logger.trace("Received initialization request")

                                    val sendingInfo = mutableMapOf<String, MutableMap<String, String>>(
                                        "commands" to mutableMapOf(),
                                        "arguments" to mutableMapOf()
                                    )
                                    val commands = commandInvoker.getCommandMap()

                                    for (command in commands.keys) {
                                        sendingInfo["commands"]!! += (command to commands[command]!!.getInfo())
                                        sendingInfo["arguments"]!! += (command to jsonCreator.objectToString(commands[command]!!.getArgsTypes()))
                                    }

                                    val answer = Answer(AnswerType.SYSTEM, jsonCreator.objectToString(sendingInfo))
                                    connectionManager.send(answer)
                                }

                                QueryType.PING -> {
                                    logger.info("Received ping request")
                                    val answer = Answer(AnswerType.SYSTEM, "Pong")
                                    connectionManager.send(answer)
                                }

                                QueryType.AUTHORIZATION -> {
                                    logger.info("Received authorization request")
                                    if (query.information == "logout") {
                                        userManager.removeToken(query.token)
                                    } else {
                                        val answer: Answer = if (userManager.userExists(query.args["username"]!!)) {
                                            val token =
                                                userManager.login(query.args["username"]!!, query.args["password"]!!)
                                            if (token.isNotEmpty()) {
                                                Answer(AnswerType.OK, "Authorized", token)
                                            } else {
                                                Answer(AnswerType.ERROR, "Wrong password")
                                            }
                                        } else {
                                            val token =
                                                userManager.register(query.args["username"]!!, query.args["password"]!!)
                                            if (token.isNotEmpty()) {
                                                Answer(AnswerType.OK, "Registered", token)
                                            } else {
                                                Answer(AnswerType.ERROR, "Could not register")
                                            }
                                        }
                                        connectionManager.send(answer)
                                    }
                                }
                            }
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

//
//        while (executeFlag) {
//            selector.select()
//            val selectedKeys = selector.selectedKeys()
//
//            if (selectedKeys.isEmpty()) continue
//
//            val iter = selectedKeys.iterator()
//            while (iter.hasNext()) {
//                val key = iter.next()
//                iter.remove()
//                if (key.isReadable) {
//                    val client = key.channel() as DatagramChannel
//                    try {
//                        connectionManager.datagramChannel = client
//                        val query = connectionManager.receive()
//
//                        when (query.queryType) {
//                            QueryType.COMMAND_EXEC -> {
//                                logger.info("Received command: ${query.information}")
//                                commandInvoker.executeCommand(query)
//                            }
//
//                            QueryType.INITIALIZATION -> {
//                                logger.trace("Received initialization request")
//
//                                val sendingInfo = mutableMapOf<String, MutableMap<String, String>>(
//                                    "commands" to mutableMapOf(),
//                                    "arguments" to mutableMapOf()
//                                )
//                                val commands = commandInvoker.getCommandMap()
//
//                                for (command in commands.keys) {
//                                    sendingInfo["commands"]!! += (command to commands[command]!!.getInfo())
//                                    sendingInfo["arguments"]!! += (command to jsonCreator.objectToString(commands[command]!!.getArgsTypes()))
//                                }
//
//                                val answer = Answer(AnswerType.SYSTEM, jsonCreator.objectToString(sendingInfo))
//                                connectionManager.send(answer)
//                            }
//
//                            QueryType.PING -> {
//                                logger.info("Received ping request")
//                                val answer = Answer(AnswerType.SYSTEM, "Pong")
//                                connectionManager.send(answer)
//                            }
//                        }
//                    } catch (e:Exception) {
//                        logger.error("Error while executing command: ${e.message}")
//                        val answer = Answer(AnswerType.ERROR, e.message.toString())
//                        connectionManager.send(answer)
//                    }
//                }
//            }
//        }
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
            fileManager.save(collectionManager)
            logger.info("Collection saved successfully")
        } catch (e:Exception) {
            logger.warn("Collection was not saved: ${e.message}")
        }
    }

    fun cleanTokens() {
        for (token in userManager.getTokens()) {
            val time = userManager.getTokenTime(token)
            if (Timestamp(System.currentTimeMillis()).time - time.time > 300000) { //300000
                userManager.removeToken(token)
            }
        }
    }
}
