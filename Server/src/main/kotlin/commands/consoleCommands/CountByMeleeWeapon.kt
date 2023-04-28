package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException

/**
 * Count by melee weapon command
 *
 * Prints the amount of elements with the provided weapon
 *
 * @property collection
 * @constructor Create command count_by_melee_weapon
 */
class CountByMeleeWeapon() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Prints the amount of elements with the provided weapon"
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
     * Calls [CommandReceiver.countByWeapon]
     */
    override fun execute(args: Map<String, String>, token: String) {
        if (Validator.verifyArgs(1, args)) {
            commandReceiver.countByWeapon(args)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }



}
