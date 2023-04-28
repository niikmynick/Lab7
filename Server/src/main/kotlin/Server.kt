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

        thread {
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

        }

        scheduleTask(60000) {//60000
            cleanTokens()
            save()
        }

        startInteractiveMode()

    }

}
