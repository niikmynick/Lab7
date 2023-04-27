import serverUtils.Console
import kotlin.concurrent.thread

fun server(actions: Console.() -> Unit) {
    val console = Console()
    console.actions()
}

/**
 * Main
 */
fun main() {

    server {
        val port = 8080
        val host = "localhost"

        initialize()

        val thread = thread {
            while (true) {
                when (readlnOrNull()) {
                    "exit" -> {
                        save()
                        stop()
                        break
                    }
                    "save" -> {
                        save()
                    }
                }
            }
        }

        start {
            startServer(host, port)

            startInteractiveMode()

            scheduleTask(1000) {
                run {
                    info("Scheduled task")
                }
            }
        }

        thread.join()
    }

}
