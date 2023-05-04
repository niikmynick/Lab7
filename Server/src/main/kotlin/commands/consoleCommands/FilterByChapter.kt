package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import utils.Answer

/**
 * Filter by chapter command
 *
 * Prints elements with the provided chapter
 *
 * @property collection
 * @constructor Create command Filter by chapter
 */
class FilterByChapter() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Prints elements with the provided chapter"
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
     * Calls [CommandReceiver.filterByChapter]
     */
    override fun execute(args: Map<String, String>, username: String): Answer {
        if (Validator.verifyArgs(2, args)) {
            return commandReceiver.filterByChapter(args)
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}
