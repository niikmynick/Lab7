import clientUtils.Console


fun client(actions: Console.() -> Unit) {
    val console = Console("172.28.16.239", 8061)
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
