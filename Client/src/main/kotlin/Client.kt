import clientUtils.Console


fun client(actions: Console.() -> Unit) {
    val console = Console("192.168.0.185", 8061) // address of the gateway
    console.actions()
}

/**
 * Main
 */

fun main() {

    client {
        connect()
        authorize()
        if (authorized) {
            startInteractiveMode()
        }

    }

}
