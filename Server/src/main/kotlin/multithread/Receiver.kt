package multithread

import collection.CollectionManager
import commands.CommandInvoker
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import serverUtils.FileManager
import tokenutils.JWTManager
import users.UserManager
import utils.*
import java.util.concurrent.LinkedBlockingQueue

class Receiver(private val taskQueue: LinkedBlockingQueue<Sending>,
               private val fileManager: FileManager,
               private val collectionManager: CollectionManager,
               private val jwtManager: JWTManager,
               private val commandInvoker: CommandInvoker,
               private val userManager: UserManager,
               private val jsonCreator: JsonCreator) : Runnable
{
    lateinit var answer : Answer
    private val logger: Logger = LogManager.getLogger(Receiver::class.java)

    override fun run() {
        val query =taskQueue.first() as Query
        val receiver = query.args["sender"]!!
        try {
            when (query.queryType) {
                QueryType.COMMAND_EXEC -> {
                    logger.info("Received command: ${query.message}")
                    fileManager.load(collectionManager)

                    if (jwtManager.validateJWS(query.token)) {
                        val username = jwtManager.retrieveUsername(query.token)
                        answer = commandInvoker.executeCommand(query, username)
                        answer.token = jwtManager.createJWS("server", username)
                        fileManager.save(collectionManager, userManager)
                    } else {
                        answer = Answer(AnswerType.AUTH_ERROR, "Unknown token. Authorize again.", receiver = receiver)
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

                    answer = Answer(AnswerType.SYSTEM, jsonCreator.objectToString(sendingInfo), receiver = receiver)
                }

                QueryType.PING -> {
                    logger.info("Received ping request from: {}", receiver)
                    answer = Answer(AnswerType.SYSTEM, "Pong", receiver = receiver)
                }

                QueryType.AUTHORIZATION -> {
                    logger.info("Received authorization request")
                    if (query.message != "logout") {
                        fileManager.load(collectionManager)
                        answer = if (userManager.userExists(query.args["username"]!!)) {
                            val token = userManager.login(query.args["username"]!!, query.args["password"]!!)
                            if (token.isNotEmpty()) {
                                Answer(AnswerType.OK, "Authorized", token, receiver = receiver)
                            } else {
                                Answer(AnswerType.ERROR, "Wrong password", receiver = receiver)
                            }
                        } else {
                            val token =
                                userManager.register(query.args["username"]!!, query.args["password"]!!)
                            if (token.isNotEmpty()) {
                                Answer(AnswerType.OK, "Registered", token, receiver = receiver)
                            } else {
                                Answer(AnswerType.ERROR, "Could not register", receiver = receiver)
                            }
                        }
                        fileManager.save(collectionManager, userManager)
                    }
                }
            }
    } catch (e: Exception) {
        logger.error("Error while executing command: ${e.message}")
        answer = Answer(AnswerType.ERROR, e.message.toString(), receiver = receiver)
    }
    }
}
