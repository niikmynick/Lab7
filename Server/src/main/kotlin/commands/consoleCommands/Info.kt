package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import utils.Answer

/**
 * Info command
 *
 * Prints info about the collection (type, creation date, amount of elements)
 *
 * @property collection
 * @constructor Create command Info
 */
class Info() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Prints info about the collection (type, creation date, amount of elements)"
    private val argsTypes = mapOf<String, String>()

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }

    /**
     * Calls [CommandReceiver.info]
     */
    override fun execute(args: Map<String, String>, username: String): Answer {
        if (Validator.verifyArgs(1, args)) {
            return commandReceiver.info(args)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}