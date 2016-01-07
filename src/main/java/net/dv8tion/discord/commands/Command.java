package net.dv8tion.discord.commands;

import java.util.List;

import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.events.message.GenericMessageEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public abstract class Command extends ListenerAdapter
{
    public abstract void onCommand(MessageReceivedEvent e, String[] args);
    public abstract List<String> getAliases();
    public abstract String getDescription();
    public abstract String getName();
    public abstract String getUsageInstructions();

    @Override
    public void onMessageReceived(MessageReceivedEvent e)
    {
        if (containsCommand(e.getMessage()))
            onCommand(e, commandArgs(e.getMessage()));
    }

    protected boolean containsCommand(Message message)
    {
        return getAliases().contains(commandArgs(message)[0]);
    }

    protected String[] commandArgs(Message message)
    {
        return commandArgs(message.getContent());
    }

    protected String[] commandArgs(String string)
    {
        return string.split(" ");
    }

    protected Message sendMessage(MessageReceivedEvent e, Message message)
    {
        if(e.isPrivate())
            return e.getPrivateChannel().sendMessage(message);
        else
            return e.getTextChannel().sendMessage(message);
    }

    protected Message sendMessage(MessageReceivedEvent e, String message)
    {
        return sendMessage(e, new MessageBuilder().appendString(message).build());
    }
}
