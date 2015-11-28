package net.dv8tion.discord.commands;

import java.util.List;

import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.Message;

public abstract class Command implements EventListener 
{
    public abstract void onChat(UserChatEvent e);
    public abstract List<String> getAliases();
    public abstract String getDescription();
    public abstract String getName();
    public abstract String getUsageInstructions();

    protected boolean containsCommand(Message message)
    {
        return getAliases().contains(commandArgs(message)[0]);
    }

    protected String[] commandArgs(Message message)
    {
        return commandArgs(message.toString());
    }

    protected String[] commandArgs(String string)
    {
        return string.split(" ");
    }
}
