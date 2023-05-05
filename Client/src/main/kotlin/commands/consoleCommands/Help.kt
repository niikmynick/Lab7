package commands.consoleCommands

import commands.CommandReceiver
import clientUtils.Validator
import exceptions.InvalidArgumentException

/**
 * Help command
 *
 * Prints info about all commands or a provided command
 *
 * @constructor Create command Help
 */
class Help(
    private val commandReceiver: CommandReceiver
) : Command() {

    override fun getInfo(): String {
        return "Prints info about all commands or a provided command"
    }

    /**
     * Calls [CommandReceiver.help]
     */
    override fun execute(args: List<String>, token: String) {
        if (Validator.verifyArgs(1, args)) {
            commandReceiver.help(args[0])
        } else if (Validator.verifyArgs(0, args)) {
            commandReceiver.help()
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}
