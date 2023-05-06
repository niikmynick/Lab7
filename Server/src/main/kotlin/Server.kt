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
        val port = 8090
        val host = "localhost"

        // address of the gateway
        val gatewayHost = "localhost"
        val gatewayPort = 8071

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
            registrationRequest(gatewayHost, gatewayPort)

        }

        scheduleTask(60000) {//60000
            //save() // does not need to save constantly, as after every action to the database, its info is automatically saved into it
        }

        startInteractiveMode()

    }

}
