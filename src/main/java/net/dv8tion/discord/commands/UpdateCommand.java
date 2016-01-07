package net.dv8tion.discord.commands;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.dv8tion.discord.Yui;
import net.dv8tion.discord.Permissions;
import net.dv8tion.discord.SettingsManager;
import net.dv8tion.discord.util.Downloader;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class UpdateCommand extends Command
{
    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        if (!Permissions.getPermissions().isOp(e.getAuthor().getId()))
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
                latestBuildDate = Yui.DATE_FORMATTER.parse(Downloader.webpage(Yui.BUILD_DATE_LATEST_URL));
                buildType = "beta";
            }
            else
            {
                latestBuildDate = Yui.DATE_FORMATTER.parse(Downloader.webpage(Yui.BUILD_DATE_RECOMMENDED_URL));
                buildType = "recommended";
            }

            SimpleDateFormat dateOutput = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a zzz");
            Date botBuildDate = Yui.getBuildDate();
            if (botBuildDate == null || botBuildDate.before(latestBuildDate))
            {
                sendMessage(e, "Updating to latest **" + buildType + "** version.\n**Latest " + buildType + " version's build date:** " + dateOutput.format(latestBuildDate));
                if (useBetaBuilds)
                    System.exit(Yui.UPDATE_LATEST_EXITCODE);
                else
                    System.exit(Yui.UPDATE_RECOMMENDED_EXITCODE);
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
