import gatewayUtils.Console

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

        start {

            startServer(host, port)

        }

        scheduleTask(60000) {//60000
        }

        startInteractiveMode()

    }

}
