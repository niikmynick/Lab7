package commands.consoleCommands

import commands.CommandReceiver
import exceptions.InvalidArgumentException
import serverUtils.Validator
import utils.Answer

class FilterByWeapon() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Prints elements with the provided weapon"
    private val argsTypes = mapOf(
        "weapon" to "MeleeWeapon"
    )
    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }

    /**
     * Calls [CommandReceiver.filterByWeapon]
     */
    override fun execute(args: Map<String, String>, username: String): Answer {
        if (Validator.verifyArgs(2, args)) {
            return commandReceiver.filterByWeapon(args)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}
