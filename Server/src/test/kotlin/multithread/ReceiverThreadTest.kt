package multithread

import collection.CollectionManager
import commands.CommandInvoker
import io.mockk.every
import io.mockk.spyk
import serverUtils.ConnectionManager
import serverUtils.DBManager
import serverUtils.FileManager
import tokenutils.JWTManager
import users.UserManager
import utils.*
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.Test
import kotlin.test.assertEquals


class ReceiverThreadTest {

    private val dbManager = DBManager("jdbc:postgresql://localhost:5432/studs", "s372819", System.getenv("dbPassword"))
    private val connectionManager = ConnectionManager()
    private val taskQueue = LinkedBlockingQueue<Sending>(10)
    private val answerQueue = LinkedBlockingQueue<Sending>(10)
    private val fileManager = spyk(FileManager(dbManager))
    private val collectionManager = CollectionManager(dbManager)
    private val jwtManager = spyk(JWTManager())
    private val commandInvoker = spyk(CommandInvoker(connectionManager))
    private val userManager = UserManager(dbManager)
    private val jsonCreator = JsonCreator()
    private val receiverThread = ReceiverThread(taskQueue, fileManager, collectionManager, jwtManager, commandInvoker, userManager, jsonCreator, answerQueue)


    @Test
    fun Pinging() {
        val query = Query(QueryType.PING, "Ping", mutableMapOf("sender" to InetSocketAddress(1).toString()))
        taskQueue.put(query)
        receiverThread.run()
        val answer = answerQueue.take() as Answer
        val expected = Answer(AnswerType.SYSTEM, "Pong", receiver = InetSocketAddress(1).toString())
        assertEquals(jsonCreator.objectToString(expected), jsonCreator.objectToString(answer), "Pinging did not return expected answer")
    }

    @Test
    fun `Authorization login and logout`() {
        every { fileManager.load(collectionManager) } returns Unit
        var query = Query(QueryType.AUTHORIZATION, "", mutableMapOf("username" to "gleb", "password" to "1234", "sender" to InetSocketAddress(1).toString()))
        taskQueue.put(query)
        receiverThread.run()
        val answer = answerQueue.take() as Answer
        val token = jwtManager.createJWS("server", "gleb")
        answer.token = token
        val expected = Answer(AnswerType.OK, "Authorized", token, receiver = InetSocketAddress(1).toString())
        assertEquals(jsonCreator.objectToString(expected), jsonCreator.objectToString(answer), "Authorization does not work as expected")

        query = Query(QueryType.AUTHORIZATION, "logout", mutableMapOf("sender" to InetSocketAddress(1).toString()), token)
        taskQueue.put(query)
        receiverThread.run()

    }
    @Test
    fun `Command execution wrong token`() {
        every { fileManager.load(collectionManager) } returns Unit

        val query = Query(QueryType.COMMAND_EXEC, "add", mutableMapOf("sender" to InetSocketAddress(1).toString()), "token")
        taskQueue.put(query)
        receiverThread.run()
        val answer = answerQueue.take() as Answer
        val expected = Answer(AnswerType.AUTH_ERROR, "Unknown token. Authorize again.", receiver = InetSocketAddress(1).toString())
        assertEquals(jsonCreator.objectToString(expected), jsonCreator.objectToString(answer), "A wrong token was accepted")
    }



    @Test
    fun `Command execution correct token`() {
        every { fileManager.load(collectionManager) } returns Unit
        every { commandInvoker.executeCommand(any(), any()) } returns Answer(AnswerType.OK, "", receiver = InetSocketAddress(1).toString())
        val token = jwtManager.createJWS("server", "gleb")
        every { jwtManager.createJWS(any(), any()) } returns token

        val query = Query(QueryType.COMMAND_EXEC, "add", mutableMapOf("sender" to InetSocketAddress(1).toString()), token)
        taskQueue.put(query)
        receiverThread.run()
        val answer = answerQueue.take() as Answer
        val expected = Answer(AnswerType.OK, "", token ,InetSocketAddress(1).toString())

        assertEquals(jsonCreator.objectToString(expected), jsonCreator.objectToString(answer), "A correct token was not accepted")
    }
}