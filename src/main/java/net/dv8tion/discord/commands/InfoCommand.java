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

import net.dv8tion.discord.YuiInfo;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class InfoCommand extends Command

{
    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        MessageBuilder builder = new MessageBuilder();
        builder.appendString("__Yui Information__\n")
                .appendString("    **Version**:       " + YuiInfo.VERSION + "\n")
                .appendString("__Creator__\n")
                .appendString("    **Name**:          DV8FromTheWorld (#6297)\n")
                .appendString("    **ID**:                107562988810027008\n")
                .appendString("    **Github**:        <http://code.dv8tion.net>\n")
                .appendString("__Development__\n")
                .appendString("    **Language**:   Java 8\n")
                .appendString("    **Library**:        JDA - v" + JDAInfo.VERSION + "\n")
                .appendString("    **Source**:        <https://github.com/DV8FromTheWorld/Yui>");
        sendMessage(e, builder.build());
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".info");
    }

    @Override
    public String getDescription()
    {
        return "Provides information about Yui.";
    }

    @Override
    public String getName()
    {
        return "Yui Information";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Arrays.asList(".info - Prints all information pertaining to the current instance of Yui.");
    }
}
