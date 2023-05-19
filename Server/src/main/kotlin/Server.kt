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
        val port = 8070
        val host = "192.168.0.185"

        // address of the gateway
        val gatewayHost = "192.168.0.185"
        val gatewayPort = 8071

        initialize()

        thread {
            while (true) {
                when (readlnOrNull()) {
                    "exit" -> {
                        connectionManager.registrationRequest(gatewayHost, gatewayPort, "Closing Server")
                        stop()
                        save()
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
            registrationRequest(gatewayHost, gatewayPort, "Registration request")

        }

        scheduleTask(60000) {//60000
            //save() // does not need to save constantly, as after every action to the database, its info is automatically saved into it
        }

        startInteractiveMode()

    }

}
