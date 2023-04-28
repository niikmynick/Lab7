package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException

/**
 * Remove any chapter command
 *
 * Deletes an element with a provided chapter value
 *
 * @property collection
 * @constructor Create command Remove any chapter
 */
class RemoveAnyChapter() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Deletes an element with a provided chapter value"
    private val argsTypes = mapOf(
        "chapter" to "Chapter"
    )

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }

    /**
     * Calls [CommandReceiver.removeByChapter]
     */
    override fun execute(args: Map<String, String>, token: String) {
        if (Validator.verifyArgs(1, args)) {
            commandReceiver.removeByChapter(args)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}
