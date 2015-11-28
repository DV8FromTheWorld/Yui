package net.dv8tion.discord.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;

public class HelpCommand extends Command
{
    private static final String NO_NAME = "No name provided for this command. Sorry!";
    private static final String NO_DESCRIPTION = "No description has been provided for this command. Sorry!";
    private static final String NO_USAGE = "No usage instructions have been provided for this command. Sorry!";

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
                String description = c.getDescription();
                description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;

                s.append("**").append(c.getAliases().get(0)).append("** - ");
                s.append(description).append("\n");
            }
            //TODO: Replace with a PrivateMessage
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": The following commands are supported by the bot\n")
                .addString(s.toString())
                .build());
        }
        else
        {
            String command = args[1].charAt(0) == '.' ? args[1] : "." + args[1];    //If there is not a preceding . attached to the command we are search, then prepend one.
            for (Command c : commands)
            {
                if (c.getAliases().contains(command))
                {
                    String name = c.getName();
                    String description = c.getDescription();
                    String usageInstructions = c.getUsageInstructions();
                    name = (name == null || name.isEmpty()) ? NO_NAME : name;
                    description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;
                    usageInstructions = (usageInstructions == null || usageInstructions.isEmpty()) ? NO_USAGE : usageInstructions;

                    //TODO: Replace with a PrivateMessage
                    e.getGroup().sendMessage(new MessageBuilder()
                        .addUserTag(e.getUser(), e.getGroup())
                        .addString(":\n")
                        .addString("**Name:** " + name + "\n")
                        .addString("**Description:** " + description + "\n")
                        .addString("**Alliases:** " + StringUtils.join(c.getAliases(), ", ") + "\n")
                        .addString("**Usage:**\n")
                        .addString(usageInstructions)
                        .build());
                    return;
                }
            }
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": The provided command '**" + args[1] + "**' does not exist. Use .help to list all commands.")
                .build());
        }
    }

    public Command registerCommand(Command command)
    {
        commands.add(command);
        return command;
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(new String[] {".help", ".commands"});
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return "Help Command";
    }

    @Override
    public String getUsageInstructions()
    {
        return null;
    }
}
