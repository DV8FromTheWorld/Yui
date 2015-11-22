package net.dv8tion.discord.commands;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;
import net.dv8tion.discord.Bot;
import net.dv8tion.discord.Downloader;

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

        try
        {
            Date latestBuildDate = Bot.DATE_FORMATTER.parse(Downloader.webpage(Bot.LATEST_BUILD_DATE_URL));
            Date botBuildDate = Bot.getBuildDate();
            if (botBuildDate == null || botBuildDate.before(latestBuildDate))
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": " + "Updating to new version with build date of: " + latestBuildDate.toString())
                    .build());
                System.exit(Bot.UPDATE_EXITCODE);
            }
            else
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": " + "The Bot is currently up-to-date.\nCurrent version's build date: " + botBuildDate.toString())
                    .build());
            }
        }
        catch (ParseException e1)
        {
            e1.printStackTrace();
        }
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
