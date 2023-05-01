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
        val portClient = 8181
        val portServer = 8080
        val host = "localhost"

        initialize()

        start {

            startGateway(host, portClient, portServer)

        }

        scheduleTask(60000) {//60000
        }

        startInteractiveMode()

    }

}
