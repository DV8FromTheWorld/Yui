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

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

public class UptimeCommand extends Command
{

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        //Taken from Almighty Alpaca
        //https://github.com/Java-Discord-Bot-System/Plugin-Uptime/blob/master/src/main/java/com/almightyalpaca/discord/bot/plugin/uptime/UptimePlugin.java#L28-L42
        final long duration = ManagementFactory.getRuntimeMXBean().getUptime();

        final long years = duration / 31104000000L;
        final long months = duration / 2592000000L % 12;
        final long days = duration / 86400000L % 30;
        final long hours = duration / 3600000L % 24;
        final long minutes = duration / 60000L % 60;
        final long seconds = duration / 1000L % 60;
        // final long milliseconds = duration % 1000;

        String uptime = (years == 0 ? "" : "**" + years + "** Years, ") + (months == 0 ? "" : "**" + months + "** Months, ") + (days == 0 ? "" : "**" + days + "** Days, ") + (hours == 0 ? "" : "**" + hours + "** Hours, ")
                + (minutes == 0 ? "" : "**" + minutes + "** Minutes, ") + (seconds == 0 ? "" : "**" + seconds + "** Seconds, ") /* + (milliseconds == 0 ? "" : milliseconds + " Milliseconds, ") */;

        uptime = replaceLast(uptime, ", ", "");
        uptime = replaceLast(uptime, ",", " and");

        sendMessage(e, "I've been online for:\n" + uptime);

    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".uptime");
    }

    @Override
    public String getDescription()
    {
        return "Displays the amount of time that the bot has been up.";
    }

    @Override
    public String getName()
    {
        return "Uptime Command";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return null;
    }

    //Taken from Almighty Alpaca
    //https://github.com/Java-Discord-Bot-System/Core/blob/master/src/main/java/com/almightyalpaca/discord/bot/system/util/StringUtils.java#L15-L17
    private String replaceLast(final String text, final String regex, final String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
}
