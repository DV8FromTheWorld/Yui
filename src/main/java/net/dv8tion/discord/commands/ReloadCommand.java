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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.dv8tion.discord.Yui;
import net.dv8tion.discord.Permissions;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class ReloadCommand extends Command
{
    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        if (!Permissions.getPermissions().isOp(e.getAuthor()))
        {
            sendMessage(e, Permissions.OP_REQUIRED_MESSAGE);
            return;
        }

        sendMessage(e, "Restarting the bot, one moment...");
        System.exit(Yui.RESTART_EXITCODE);
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".reload");
    }

    @Override
    public String getDescription()
    {
        return "Kills the current instance and launches a fresh instance of this bot.";
    }

    @Override
    public String getName()
    {
        return "Bot Reload/Restart Command";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
                ".reload\n"
                + "If you are running the bot without the bootloader, this command will be disabled.\n"
                + "The bootloader is required to relaunch the bot.");
    }
}
