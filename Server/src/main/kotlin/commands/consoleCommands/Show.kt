package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import utils.Answer

/**
 * Show command
 *
 * Prints all elements of the collection
 *
 * @constructor Create command Show
 */
class Show() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Prints all elements of the collection"
    private val argsTypes = mapOf<String, String>()

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }

    /**
     * Calls [CommandReceiver.show]
     */
    override fun execute(args: Map<String, String>, username: String): Answer {
        if (Validator.verifyArgs(1, args)) {
            return commandReceiver.show(args)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }

}