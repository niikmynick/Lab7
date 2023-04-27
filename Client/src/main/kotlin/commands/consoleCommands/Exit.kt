package commands.consoleCommands

import clientUtils.Validator
import exceptions.InvalidArgumentException

/**
 * Exit command
 *
 * @constructor Create command Exit
 */
class Exit: Command() {

    override fun getInfo(): String {
        return "Exits the app (without saving data)"
    }

    /**
     * Sets execution flag to false
     */
    override fun execute(args: List<String>) {
        if (Validator.verifyArgs(0, args)) {
            setFlag(false)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}