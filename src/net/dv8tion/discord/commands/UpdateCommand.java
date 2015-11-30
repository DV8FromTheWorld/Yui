package net.dv8tion.discord.commands;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;
import net.dv8tion.discord.Bot;
import net.dv8tion.discord.Downloader;
import net.dv8tion.discord.Permissions;

public class UpdateCommand extends Command
{
    @Override
    public void onChat(UserChatEvent e)
    {
        if (!containsCommand(e.getMsg()))
            return;

        if (!Permissions.getPermissions().isOp(e.getUser().getUser().getId()))
        {
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": " + Permissions.OP_REQUIRED_MESSAGE)
                .build());
            return;
        }

        try
        {
            SimpleDateFormat dateOutput = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a zzz");
            Date latestBuildDate = Bot.DATE_FORMATTER.parse(Downloader.webpage(Bot.LATEST_BUILD_DATE_URL));
            Date botBuildDate = Bot.getBuildDate();
            if (botBuildDate == null || botBuildDate.before(latestBuildDate))
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": " + "Updating to latest version.\n**Latest version's build date:** " + dateOutput.format(latestBuildDate))
                    .build());
                System.exit(Bot.UPDATE_EXITCODE);
            }
            else
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": " + "The Bot is currently up-to-date.\n**Current version's build date:** " + dateOutput.format(botBuildDate))
                    .build());
            }
        }
        catch (ParseException e1)
        {
            e1.printStackTrace();
        }
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(new String[] {".update"});
    }

    @Override
    public String getDescription()
    {
        return "Used to update the Bot to the latest available version.";
    }

    @Override
    public String getName()
    {
        return "Bot Update Command";
    }

    @Override
    public String getUsageInstructions()
    {
        return ".update\n"
                + "If you are running the bot without the bootloader, this command will be disabled.\n"
                + "The bootloader is required to update the bot.";
    }
}
