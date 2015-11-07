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
        String[] args = e.getMsg().getMessage().split(" ");
        if (aliases().contains(args[0])){
            e.getGroup().sendMessage(new MessageBuilder()
                    .addString("Yes, ")
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString("?")
                    .build());
        }
        System.out.println((e.getMsg().isEdited() ? "# " : "") + "[" + e.getGroup().getName() + "] " + e.getUser() + " > " + e.getMsg().getMessage());
    }

    @Override
    public List<String> aliases()
    {
        return Arrays.asList(".t", ".test");
    }

    @Override
    public String commandDescription()
    {
        return "Just a test method, haven't removed yet for some reason";
    }

    @Override
    public String helpMessage()
    {
        return "This B Only a Test!";
    }
}
