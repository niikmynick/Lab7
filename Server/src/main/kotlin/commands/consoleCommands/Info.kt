package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException

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
    override fun execute(args: Map<String, String>, token: String) {
        if (Validator.verifyArgs(0, args)) {
            commandReceiver.info()
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}