package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import utils.Answer

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
    override fun execute(args: Map<String, String>, username: String): Answer {
        if (Validator.verifyArgs(1, args)) {
            return commandReceiver.clear(args, username)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}