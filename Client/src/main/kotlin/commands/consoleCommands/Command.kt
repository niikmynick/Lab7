package commands.consoleCommands

import kotlinx.serialization.Serializable

/**
 * Command
 *
 * @constructor Create empty Command
 */

@Serializable
abstract class Command {
    private var executionFlag = true
    fun setFlag(flag:Boolean) {
        this.executionFlag = flag
    }

    fun getExecutionFlag(): Boolean {
        return executionFlag
    }

    /**
     * Get info
     *
     * @return
     */
    abstract fun getInfo(): String

    /**
     * Execute
     *
     * @return
     */
    abstract fun execute(args: List<String>, token: String)
}