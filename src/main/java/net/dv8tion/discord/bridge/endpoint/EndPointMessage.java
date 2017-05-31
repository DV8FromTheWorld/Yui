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
package net.dv8tion.discord.bridge.endpoint;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.MessageEvent;

public class EndPointMessage
{
    private EndPointType messageType;
    private String senderName;
    private String message;

    // -- Discord specific --
    private User discordUser;
    private Message discordMessage;

    // -- irc specific --
    private MessageEvent<? extends PircBotX> ircEvent;
    private org.pircbotx.User ircUser;

    private EndPointMessage() {}

    public static EndPointMessage create(String senderName, String message)
    {
        return create(EndPointType.UNKNOWN, senderName, message);
    }

    public static EndPointMessage create(EndPointType messageType, String senderName, String msg)
    {
        EndPointMessage message = new EndPointMessage();
        message.messageType = messageType;
        message.senderName = senderName;
        message.message = msg;
        return message;
    }

    public static EndPointMessage createFromDiscordEvent(Message msg)
    {
        EndPointMessage message = new EndPointMessage();
        message.messageType = EndPointType.DISCORD;
        message.setDiscordMessage(msg);
        message.senderName = msg.getAuthor().getName();
        message.discordUser = msg.getAuthor();
        return message;
    }

    public static EndPointMessage createFromIrcEvent(MessageEvent<? extends PircBotX> event)
    {
        EndPointMessage message = new EndPointMessage();
        message.message = event.getMessage();
        message.messageType = EndPointType.IRC;
        message.senderName = event.getUser().getNick();
        message.ircEvent = event;
        message.ircUser = event.getUser();
        return message;
    }

    public String getSenderName()
    {
        return senderName;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public EndPointType getType()
    {
        return messageType;
    }

    // ------ Discord Specific ------

    public User getDiscordUser()
    {
        if (!messageType.equals(EndPointType.DISCORD))
            throw new IllegalStateException("Attempted to get Discord user from a non-Discord message");
        return discordUser;
    }

    public Message getDiscordMessage()
    {
        if (!messageType.equals(EndPointType.DISCORD))
            throw new IllegalStateException("Attempted to get Discord message from a non-Discord message");
        return discordMessage;
    }

    public void setDiscordMessage(Message discordMessage)
    {
        String parsedMessage = discordMessage.getContent();
        for (Message.Attachment attach : discordMessage.getAttachments())
        {
            parsedMessage += "\n" + attach.getUrl();
        }

        this.message = parsedMessage;
        this.discordMessage = discordMessage;
    }

    // ------ IRC Specific ------

    public MessageEvent<? extends PircBotX> getIrcEvent()
    {
        if (!messageType.equals(EndPointType.IRC))
            throw new IllegalStateException("Attemped to get IRC event for non-IRC message");
        return ircEvent;
    }

    public org.pircbotx.User getIrcUser()
    {
        if (!messageType.equals(EndPointType.IRC))
            throw new IllegalStateException("Attemped to get IRC user for non-IRC message");
        return ircUser;
    }
}
