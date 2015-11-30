package net.dv8tion.discord.commands;

import java.util.Arrays;
import java.util.List;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;

public class TestCommand extends Command
{
    @Override
    public void onChat(UserChatEvent e)
    {
        if (containsCommand(e.getMsg()))
        {
            e.getGroup().sendMessage(new MessageBuilder()
                    .addString("Yes, ")
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString("?")
                    .build());
        }
        if (e.getServer().getId().equals("107563502712954880")) //The Gaming Bunch's ServerId
            System.out.println((e.getMsg().isEdited() ? "# " : "") + "[" + e.getGroup().getName() + "] " + e.getUser() + " > " + e.getMsg().getMessage());
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
    public String getUsageInstructions()
    {
        return ".test\n"
                + "Really, this command is just a test, however, it will @Mention you.";
    }
}
