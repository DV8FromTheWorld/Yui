package net.dv8tion.discord.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;

public class HelpCommand extends Command
{

    private ArrayList<Command> commands;

    public HelpCommand()
    {
        commands = new ArrayList<Command>();
    }

    @Override
    public void onChat(UserChatEvent e)
    {
        if (!containsCommand(e.getMsg()))
            return;

        String[] args = commandArgs(e.getMsg());
        if (args.length < 2)
        {
            StringBuilder s = new StringBuilder();
            for (Command c : commands)
            {
                s.append("**").append(c.aliases().get(0)).append("** - ");
                s.append(c.commandDescription()).append("\n");
            }
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": The following commands are supported by the bot\n")
                .addString(s.toString())
                .build());
        }
        else
        {

        }
    }

    public Command registerCommand(Command command)
    {
        commands.add(command);
        return command;
    }

    @Override
    public List<String> aliases()
    {
        return Arrays.asList(new String[] {".help"});
    }

    @Override
    public String commandDescription()
    {
        return null;
    }

    @Override
    public String helpMessage()
    {
        return null;
    }

}
