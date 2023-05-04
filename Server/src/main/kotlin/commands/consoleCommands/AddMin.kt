package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import utils.Answer

/**
 * Add min command
 *
 * Adds a new element into the collection if its value is lower than the lowest element in the collection
 *
 * @property collection
 * @constructor Create command Add min
 */
class AddMin() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Adds a new element into the collection if its value is lower than the lowest element in the collection"
    private val argsTypes = mapOf(
        "spaceMarine" to "SpaceMarine"
    )
    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }

    /**
     * Calls [CommandReceiver.addMin]
     */
    override fun execute(args: Map<String, String>, username: String): Answer {
        if (Validator.verifyArgs(2, args)) {
            return commandReceiver.addMin(args, username)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}