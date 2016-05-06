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

import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.events.message.GenericMessageEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.events.message.priv.GenericPrivateMessageEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestCommand extends Command
{
    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        sendMessage(e, new MessageBuilder()
                .appendString("Yes, ")
                .appendMention(e.getAuthor())
                .appendString("?")
                .build());
    }

    @Override
    public void onGenericMessage(GenericMessageEvent e)
    {
        //Don't care about Delete and Embed events. (both have null messages).
        if (e.getMessage() == null)
            return;

        if (e instanceof GenericGuildMessageEvent)
        {
            GenericGuildMessageEvent event = (GenericGuildMessageEvent) e;
            if (event.getGuild().getId().equals("107563502712954880"))  //Gaming Bunch Guild Id
                System.out.println((event.getMessage().isEdited() ? "# " : "") + "[#" + event.getChannel().getName() + "] <" + event.getAuthor().getUsername() + "> " + event.getMessage().getContent());
        }

        if (e instanceof GenericPrivateMessageEvent)
            System.out.println((e.getMessage().isEdited() ? "# " : "") + "[Private Message] <" + e.getAuthor().getUsername() + "> " + e.getMessage().getContent());
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".t", ".test");
    }

    @Override
    public String getDescription()
    {
        return "Just a test method, haven't removed yet for some reason";
    }

    @Override
    public String getName()
    {
        return "Test Command";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
                ".test\n"
                + "Really, this command is just a test, however, it will @Mention you.");
    }
}
