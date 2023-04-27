package clientUtils

import commands.*
import commands.consoleCommands.*
import exceptions.InvalidInputException
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

class Console {
    private val connectionManager = ConnectionManager("localhost", 8080)

    private val outputManager = OutputManager()
    private val inputManager = InputManager(outputManager)

    private val commandInvoker = CommandInvoker(outputManager)
    private val commandReceiver = CommandReceiver(commandInvoker, outputManager, inputManager, connectionManager)

    private val jsonCreator = JsonCreator()

    private val logger: Logger = LogManager.getLogger(Console::class.java)


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

    private fun registerBasicCommands() {
        commandInvoker.register("help", Help(commandReceiver))
        commandInvoker.register("exit", Exit())
        commandInvoker.register("execute_script", ScriptFromFile(commandReceiver))
        logger.debug("Registered basic client commands")
    }

    /**
     * Registers commands and waits for user prompt
     */
    private fun initialize() {
        val query = Query(QueryType.INITIALIZATION, "", mapOf())
        val answer = connectionManager.checkedSendReceive(query)
        logger.debug("Sent initialization query")
        if (answer.answerType == AnswerType.ERROR) {
            outputManager.println(answer.message)
        } else {
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

        registerBasicCommands()
    }

    fun startInteractiveMode() {
        var executeFlag: Boolean? = true
        outputManager.surePrint("Waiting for user prompt ...")

        do {
            try {
                outputManager.print("$ ")
                val query = inputManager.read().trim().split(" ")
                if (query[0] != "") {
                    commandInvoker.executeCommand(query)
                    executeFlag = commandInvoker.getCommandMap()[query[0]]?.getExecutionFlag()
                    checkConnection()
                }

            } catch (e: InvalidInputException) {
                outputManager.surePrint(e.message)
                logger.warn(e.message)
            } catch (e: Exception) {
                outputManager.surePrint(e.message.toString())
                logger.warn(e.message)
            }

        } while (executeFlag != false)
    }
}
