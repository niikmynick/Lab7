package commands.consoleCommands

import kotlinx.serialization.Serializable

/**
 * Command
 *
 * @constructor Create empty Command
 */

@Serializable
abstract class Command {

    /**
     * Get info
     *
     * @return
     */
    abstract fun getInfo(): String
    abstract fun getArgsTypes(): Map<String, String>

    /**
     * Execute
     *
     * @return
     */
    abstract fun execute(args: Map<String, String>, token: String)
}