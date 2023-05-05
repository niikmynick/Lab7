package clientUtils

import clientUtils.readers.StringReader
import commands.*
import commands.consoleCommands.*
import exceptions.InvalidInputException
import exceptions.NotAuthorized
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.*

/**
 * Class that handles commands and provides them all needed parameters
 * @property outputManager
 * @property inputManager
 * @property commandInvoker
 * @property commandReceiver
 */

class Console(private var host: String, private var port: Int) {
    private var connectionManager = ConnectionManager(host, port)

    private val outputManager = OutputManager()
    private val inputManager = InputManager(outputManager)

    private val commandInvoker = CommandInvoker(outputManager)
    private val commandReceiver = CommandReceiver(commandInvoker, outputManager, inputManager, connectionManager)

    private val jsonCreator = JsonCreator()

    private val logger: Logger = LogManager.getLogger(Console::class.java)

    /**
     * Contains the token that authorizes the client to execute commands in the server. It is initialized as an empty string.
     */
    private var token = ""
    var authorized: Boolean = false

    /**
     * Attempts to connect to a server.
     * If connection is successful, it will initialize the connection by calling the [initialize] function.
     * If connection fails, it will prompt the user to retry or register basic commands.
     */
    fun connect() {
        if (connectionManager.connected()) {
            logger.debug("Connected to server")
            outputManager.println("Connected to server")
            initialize()
        } else {
            outputManager.println("No server connection")
            logger.warn("No server connection")
            outputManager.println("Retry connection? [y/n]")
            outputManager.print("$ ")
            var query = inputManager.read().trim().lowercase().split(" ")
            while ((query[0] != "y") and (query[0] != "n")) {
                outputManager.println("Wrong input\nRetry connection? [y/n]")
                outputManager.print("$ ")
                query = inputManager.read().trim().lowercase().split(" ")
            }
            if (query[0] == "y") {
                connect()
            } else {
                registerBasicCommands()
            }
        }
    }

    /**
     * Checks if a connection with the server is active with [ConnectionManager.connected]
     * If the connection is active, it returns true.
     * If the connection is not active, it logs the event, prompts the user to reconnect and returns false.
     */
    private fun checkConnection(): Boolean {
        return if (connectionManager.connected()) {
            true
        } else {
            logger.warn("Connection with server is dead")
            outputManager.println("Connection with server is dead")
            connect()
            false
        }
    }

    /**
     * Registers the basic commands that can be executed without the server
     */
    private fun registerBasicCommands() {
        commandInvoker.register("help", Help(commandReceiver))
        commandInvoker.register("exit", Exit(connectionManager))
        commandInvoker.register("execute_script", ScriptFromFile(commandReceiver))
        logger.debug("Registered basic client commands")
    }

    /**
     * Sends an Initialization request to the server, then receives and registers
     * all commands with their respective needed arguments.
     */
    private fun initialize() {
        val query = Query(QueryType.INITIALIZATION, "", mutableMapOf())
        val answer = connectionManager.checkedSendReceive(query)
        logger.debug("Sent initialization query")
        when (answer.answerType) {
            AnswerType.ERROR -> outputManager.println(answer.message)
            AnswerType.AUTH_ERROR -> {
                outputManager.println(answer.message)
                authorize()
            }
            AnswerType.SYSTEM -> {
                val serverCommands = jsonCreator.stringToObject<Map<String, Map<String, String>>>(answer.message)
                logger.info("Received commands from server: ${serverCommands["commands"]!!.keys.toList()}")

                commandInvoker.clearCommandMap()

                for (i in serverCommands["commands"]!!.keys) {
                    commandInvoker.register(
                        i,
                        UnknownCommand(
                            commandReceiver,
                            i,
                            serverCommands["commands"]!![i]!!,
                            jsonCreator.stringToObject(serverCommands["arguments"]!![i]!!)
                        )
                    )
                }
            }
            else -> {
                outputManager.println("Unknown answer type")
                logger.warn("Unknown answer type")
            }
        }

        registerBasicCommands()
    }

    /**
     * Requests the user to authorize.
     * The function recursively asks for username and password until an Authorized message from the server is received.
     * Finally, saves the received token into the [token] variable.
     */
    fun authorize(){
        outputManager.surePrint("Login or register to use the collection: ")
        val username = StringReader(outputManager, inputManager).read("Username: ")
        val password = StringReader(outputManager, inputManager).read("Password: ")
        val query = Query(QueryType.AUTHORIZATION, "", mutableMapOf("username" to username, "password" to password))
        val answer = connectionManager.checkedSendReceive(query)
        logger.debug("Sent authorization query")
        if (answer.answerType == AnswerType.ERROR) {
            outputManager.println(answer.message)
            authorize()
        } else {
            logger.debug("Authorized")
            authorized = true
            token = answer.token
        }
    }

    /**
     * Starts the interactive mode which asks for prompt until an [Exit] command is executed
     */
    fun startInteractiveMode() {
        var executeFlag: Boolean? = true
        outputManager.surePrint("Waiting for user prompt ...")

        do {
            try {
                outputManager.print("$ ")
                val query = inputManager.read().trim().split(" ")
                if (query[0] != "") {
                    checkConnection()
                    commandInvoker.executeCommand(query, token)
                    executeFlag = commandInvoker.getCommandMap()[query[0]]?.getExecutionFlag()
                }

            } catch (e: InvalidInputException) {
                outputManager.surePrint(e.message)
                logger.warn(e.message)
            } catch (e: NotAuthorized) {
                authorize()
            }
            catch (e: Exception) {
                outputManager.surePrint(e.message.toString())
                logger.warn(e.message)
            }

        } while (executeFlag != false)
    }
}
