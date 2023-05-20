import gatewayUtils.ConnectionManager
import io.mockk.every
import io.mockk.spyk
import multithread.FromServerThread
import utils.Answer
import utils.AnswerType
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.Test


class GatewayTest {
    private val connectionManager = spyk<ConnectionManager>()
    private val answerQueue = LinkedBlockingQueue<Answer>()

    private val serverThread = FromServerThread(connectionManager, answerQueue)

    @Test
    fun `Receiving multiple servers`() {
        every { connectionManager.receiveFromServer() } returns Answer(AnswerType.REGISTRATION_REQUEST, "Registration request", receiver = "")
        for (i in 1..5) {
            connectionManager.remoteAddressServer = InetSocketAddress(i)
            val received = connectionManager.receiveFromServer()
            answerQueue.put(received)
            serverThread.run()
        }
        assert(answerQueue.isEmpty()) { "AnswerQueue still contains answers" }
        assert(connectionManager.availableServers.size == 5) { "Queue of available servers does not match with amount of sent requests" }
    }

    @Test
    fun `Removing servers from available servers`() {
        every { connectionManager.receiveFromServer() } returns Answer(AnswerType.REGISTRATION_REQUEST, "Closing Server", receiver = "")
        for (i in 1..5) {
            connectionManager.availableServers.add(InetSocketAddress(i))
            println(connectionManager.availableServers)
        }
        for (i in 1..5) {
            connectionManager.remoteAddressServer = InetSocketAddress(i)
            val received = connectionManager.receiveFromServer()
            answerQueue.put(received)
            serverThread.run()
            println(connectionManager.availableServers)
        }
        assert(answerQueue.isEmpty()) { "AnswerQueue still contains answers" }
        assert(connectionManager.availableServers.isEmpty()) { "Queue of available servers should be 0" }
    }
}