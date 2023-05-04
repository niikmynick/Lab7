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
        val portClient = 8060
        val portServer = 8070
        val portPing = 8080
        val host = "localhost"


        start {

            startGateway(host, portClient, portServer, portPing)

        }

        scheduleTask(60000) {//60000
        }

        startInteractiveMode()


    }

}
