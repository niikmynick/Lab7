package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException

/**
 * Update command
 *
 * Update values of the element with the provided id
 *
 * @constructor Create command Update
 */
class Update() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Updates values of the element with the provided id"
    private val argsTypes = mapOf(
        "id" to "Long",
        "spaceMarine" to "SpaceMarine"
    )

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }

    /**
     * Calls [CommandReceiver.updateByID] with provided id
     */
    override fun execute(args: Map<String, String>, username: String) {
        if (Validator.verifyArgs(2, args)) {
            try {
                commandReceiver.updateByID(args, username)
            } catch (e:Exception) {
                throw InvalidArgumentException("Expected an argument but it was not found")
            }
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }

}