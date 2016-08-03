/**
 *     Copyright 2015-2016 Austin Keener
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.discord.commands;

import net.dv8tion.discord.Permissions;
import net.dv8tion.discord.SettingsManager;
import net.dv8tion.discord.Yui;
import net.dv8tion.discord.YuiInfo;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

        if(SettingsManager.getInstance().getSettings().getUseBetaBuilds() && YuiInfo.hasNewBetaVersion())
        {
            sendMessage(e, new MessageBuilder()
                .appendString("Updating to the latest **beta** version.\n")
                .appendString(YuiInfo.VERSION.toString())
                .appendString(" -> ")
                .appendString(YuiInfo.getLatestBetaVersion().toString())
                .build());
            System.exit(Yui.UPDATE_LATEST_EXITCODE);
        }
        else if (YuiInfo.hasNewRecommendedVersion())
        {
            sendMessage(e, new MessageBuilder()
                    .appendString("Updating to the latest **recommended** version.\n")
                    .appendString(YuiInfo.VERSION.toString())
                    .appendString(" -> ")
                    .appendString(YuiInfo.getLatestRecommendedVersion().toString())
                    .build());
            System.exit(Yui.UPDATE_RECOMMENDED_EXITCODE);
        }
        else
        {
            sendMessage(e, new MessageBuilder()
                .appendString("Yui is currently up-to-date compared to the latest ")
                .appendString(SettingsManager.getInstance().getSettings().getUseBetaBuilds() ? "beta" : "recommended")
                .appendString("build.\n")
                .appendString("Current version: ", MessageBuilder.Formatting.BOLD)
                .appendString(YuiInfo.VERSION.toString())
                .build());
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
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
                ".update\n"
                + "If you are running the bot without the bootloader, this command will be disabled.\n"
                + "The bootloader is required to update the bot.");
    }
}
