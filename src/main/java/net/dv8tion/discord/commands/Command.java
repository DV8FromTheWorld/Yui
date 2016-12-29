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

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;

public abstract class Command extends ListenerAdapter
{
    public abstract void onCommand(MessageReceivedEvent e, String[] args);
    public abstract List<String> getAliases();
    public abstract String getDescription();
    public abstract String getName();
    public abstract List<String> getUsageInstructions();

    @Override
    public void onMessageReceived(MessageReceivedEvent e)
    {
        if (e.getAuthor().isBot() && !respondToBots())
            return;
        if (containsCommand(e.getMessage()))
            onCommand(e, commandArgs(e.getMessage()));
    }

    protected boolean containsCommand(Message message)
    {
        return getAliases().contains(commandArgs(message)[0]);
    }

    protected String[] commandArgs(Message message)
    {
        return commandArgs(message.getContent());
    }

    protected String[] commandArgs(String string)
    {
        return string.split(" ");
    }

    protected Message sendMessage(MessageReceivedEvent e, Message message)
    {
        if(e.isFromType(ChannelType.PRIVATE))
            return e.getPrivateChannel().sendMessage(message).complete();
        else
            return e.getTextChannel().sendMessage(message).complete();
    }

    protected Message sendMessage(MessageReceivedEvent e, String message)
    {
        return sendMessage(e, new MessageBuilder().append(message).build());
    }

    protected boolean respondToBots()
    {
        return false;
    }
}
