import clientUtils.Console


fun client(actions: Console.() -> Unit) {
    val console = Console("localhost", 8061)
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
