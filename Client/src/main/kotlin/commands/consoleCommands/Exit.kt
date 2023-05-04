package commands.consoleCommands

import clientUtils.ConnectionManager
import clientUtils.Validator
import exceptions.InvalidArgumentException
import utils.Query
import utils.QueryType

/**
 * Exit command
 *
 * @constructor Create command Exit
 */
class Exit(private val connectionManager: ConnectionManager): Command() {

    override fun getInfo(): String {
        return "Exits the app (without saving data)"
    }

    /**
     * Sets execution flag to false
     */
    override fun execute(args: List<String>, token: String) {
        if (Validator.verifyArgs(0, args)) {
            setFlag(false)
            connectionManager.send(Query(QueryType.AUTHORIZATION, "logout", mutableMapOf(), token))
        } else throw InvalidArgumentException("Invalid arguments were entered. Use HELP command to check")
    }
}