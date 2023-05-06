package multithread

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import serverUtils.ConnectionManager
import utils.Answer
import utils.AnswerType
import utils.Sending
import java.util.concurrent.LinkedBlockingQueue

class SenderThread(private val answerQueue: LinkedBlockingQueue<Sending>,
                   private val connectionManager: ConnectionManager) : Runnable {
    var answer = Answer(AnswerType.ERROR, "Unknown error", receiver = "", token = "")
    private val logger: Logger = LogManager.getLogger(SenderThread::class.java)
    override fun run() {
        connectionManager.send(answerQueue.take() as Answer)
    }

}