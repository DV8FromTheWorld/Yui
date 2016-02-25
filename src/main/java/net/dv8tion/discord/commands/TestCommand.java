package net.dv8tion.discord.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.events.message.GenericMessageEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.events.message.priv.GenericPrivateMessageEvent;

public class TestCommand extends Command
{
    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        sendMessage(e, new MessageBuilder()
                .appendString("Yes, ")
                .appendMention(e.getAuthor())
                .appendString("?")
                .build());
    }

    @Override
    public void onGenericMessage(GenericMessageEvent e)
    {
        //Don't care about Delete and Embed events. (both have null messages).
        if (e.getMessage() == null)
            return;

        if (e instanceof GenericGuildMessageEvent)
        {
            GenericGuildMessageEvent event = (GenericGuildMessageEvent) e;
            if (event.getGuild().getId().equals("107563502712954880"))  //Gaming Bunch Guild Id
                System.out.println((event.getMessage().isEdited() ? "# " : "") + "[#" + event.getChannel().getName() + "] <" + event.getAuthor().getUsername() + "> " + event.getMessage().getContent());
        }

        if (e instanceof GenericPrivateMessageEvent)
            System.out.println((e.getMessage().isEdited() ? "# " : "") + "[Private Message] <" + e.getAuthor().getUsername() + "> " + e.getMessage().getContent());
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".t", ".test");
    }

    @Override
    public String getDescription()
    {
        return "Just a test method, haven't removed yet for some reason";
    }

    @Override
    public String getName()
    {
        return "Test Command";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
                ".test\n"
                + "Really, this command is just a test, however, it will @Mention you.");
    }
}
