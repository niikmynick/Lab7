package commands.consoleCommands

import commands.CommandReceiver


class UnknownCommand(
    private val commandReceiver: CommandReceiver,
    private val name: String,
    private val info: String,
    private val argsTypes:Map<String, String>
) : Command() {

    override fun getInfo(): String {
        return info
    }

    override fun execute(args: List<String>) {
            commandReceiver.unknownCommand(name, argsTypes)
    }
}
