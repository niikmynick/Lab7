import clientUtils.Console

/**
 * Main
 */

fun main() {
    val console = Console()

    console.getConnection()

    console.initialize()
    console.startInteractiveMode()
}
