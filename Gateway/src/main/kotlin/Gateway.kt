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
        val portClient = 8060 // give this to client
        val portServer = 8070 // give this to server
        val portPing = 8080
        val host = "172.28.16.239"


        start {

            startGateway(host, portClient, portServer, portPing)

        }

        scheduleTask(60000) {//60000
        }

        startInteractiveMode()


    }

}
