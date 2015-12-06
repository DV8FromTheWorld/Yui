package net.dv8tion.discord.commands;

import java.util.List;

import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.Message;
import me.itsghost.jdiscord.message.MessageBuilder;

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

    protected void sendMessage(UserChatEvent e, String message)
    {
        if (e.getServer() != null)
        {
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": " + message)
                .build());
        }
        else //This is a PM
        {
            e.getGroup().sendMessage(new MessageBuilder()
                .addString(message)
                .build());
        }
    }
}
