package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException

/**
 * Clear command
 *
 * Clears all elements in the collection
 * 
 * @property collection
 * @constructor Create command Clear
 */
class Clear() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Clears all elements in the collection"
    private val argsTypes = mapOf<String, String>()

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }

    /**
     * Calls [CommandReceiver.clear]
     */
    override fun execute(args: Map<String, String>, token: String) {
        if (Validator.verifyArgs(0, args)) {
            commandReceiver.clear()
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}