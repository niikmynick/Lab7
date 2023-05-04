package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import utils.Answer


/**
 * Add command
 */
class Add() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Adds a new element into the collection"
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
     * Calls [CommandReceiver.add]
     */
    override fun execute(args: Map<String, String>, username: String): Answer {
        if (Validator.verifyArgs(2, args)) {
            return commandReceiver.add(args, username)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}
