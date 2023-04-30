package commands.consoleCommands

import commands.CommandReceiver
import clientUtils.Validator
import exceptions.InvalidArgumentException

/**
 * Script from file command
 *
 * Reads and executes script from provided file (The script should have the same commands used in the interactive mode
 *
 * @constructor Create command Script from file
 */
class ScriptFromFile(
    private val commandReceiver: CommandReceiver
): Command() {

    override fun getInfo(): String {
        return "Reads and executes script from provided file (The script should have the same commands used in the interactive mode)"
    }

    /**
     * Calls [CommandReceiver.executeScript]
     */
    override fun execute(args: List<String>, token: String) {
        if (Validator.verifyArgs(1, args)) {
            try {
                commandReceiver.executeScript(args[0])
            } catch (e:IndexOutOfBoundsException) {
                throw InvalidArgumentException("Expected an argument but it was not found")
            }
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }

}