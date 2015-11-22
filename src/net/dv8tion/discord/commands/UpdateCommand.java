package net.dv8tion.discord.commands;

import java.util.Arrays;
import java.util.List;

import me.itsghost.jdiscord.events.UserChatEvent;
import net.dv8tion.discord.Bot;

public class UpdateCommand extends Command
{
    @Override
    public void onChat(UserChatEvent e)
    {
        String[] args = e.getMsg().toString().split(" ");
        if (!aliases().contains(args[0]))
        {
            return;
        }

        //TODO: Check permission - admin

        System.exit(Bot.UPDATE_EXITCODE);
    }

    @Override
    public List<String> aliases()
    {
        return Arrays.asList(new String[] {".update"});
    }

    @Override
    public String commandDescription()
    {
        return "Used to update the Bot to the latest available version.";
    }

    @Override
    public String helpMessage()
    {
        return null;
    }
}
