import clientUtils.Console

/**
 * Main
 */

fun main() {
    val console = Console()

    console.connect()
    console.authorize()

    console.startInteractiveMode()
}
