package net.dv8tion.discord.commands;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import me.itsghost.jdiscord.events.UserChatEvent;
import net.dv8tion.discord.Bot;
import net.dv8tion.discord.Permissions;
import net.dv8tion.discord.SettingsManager;
import net.dv8tion.discord.util.Downloader;

public class UpdateCommand extends Command
{
    @Override
    public void onChat(UserChatEvent e)
    {
        if (!containsCommand(e.getMsg()))
            return;

        if (!Permissions.getPermissions().isOp(e.getUser().getUser().getId()))
        {
            sendMessage(e, Permissions.OP_REQUIRED_MESSAGE);
            return;
        }

        try
        {
            boolean useBetaBuilds = SettingsManager.getInstance().getSettings().getUseBetaBuilds();
            Date latestBuildDate = null;
            String buildType;
            if (useBetaBuilds)
            {
                latestBuildDate = Bot.DATE_FORMATTER.parse(Downloader.webpage(Bot.BUILD_DATE_LATEST_URL));
                buildType = "beta";
            }
            else
            {
                latestBuildDate = Bot.DATE_FORMATTER.parse(Downloader.webpage(Bot.BUILD_DATE_RECOMMENDED_URL));
                buildType = "recommended";
            }

            SimpleDateFormat dateOutput = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a zzz");
            Date botBuildDate = Bot.getBuildDate();
            if (botBuildDate == null || botBuildDate.before(latestBuildDate))
            {
                sendMessage(e, "Updating to latest **" + buildType + "** version.\n**Latest " + buildType + " version's build date:** " + dateOutput.format(latestBuildDate));
                if (useBetaBuilds)
                    System.exit(Bot.UPDATE_LATEST_EXITCODE);
                else
                    System.exit(Bot.UPDATE_RECOMMENDED_EXITCODE);
            }
            else
            {
                sendMessage(e, "The Bot is currently up-to-date compared to the latest " + buildType + " build.\n**Current version's build date:** " + dateOutput.format(botBuildDate));
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
        return Arrays.asList(".update");
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
