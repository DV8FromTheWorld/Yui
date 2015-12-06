package net.dv8tion.discord.bridge.endpoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.Message;
import me.itsghost.jdiscord.talkable.Group;
import net.dv8tion.discord.Bot;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.MessageEvent;

public class EndPointMessage
{
    private EndPointType messageType;
    private String senderName;
    private String message;

    // -- Discord specific --
    private UserChatEvent discordEvent;
    private me.itsghost.jdiscord.talkable.User discordUser;
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

    public static EndPointMessage createFromDiscordEvent(UserChatEvent event)
    {
        EndPointMessage message = new EndPointMessage();
        message.messageType = EndPointType.DISCORD;
        message.setDiscordMessage(event.getMsg());
        message.senderName = event.getUser().getUser().getUsername();
        message.discordEvent = event;
        message.discordUser = event.getUser().getUser();
        message.discordMessage = event.getMsg();
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

    public me.itsghost.jdiscord.talkable.User getDiscordUser()
    {
        if (!messageType.equals(EndPointType.DISCORD))
            throw new IllegalStateException("Attempted to get Discord user from a non-Discord message");
        return discordUser;
    }

    public UserChatEvent getDiscordEvent()
    {
        if (!messageType.equals(EndPointType.DISCORD))
            throw new IllegalStateException("Attemped to get Discord event for non-Discord message");
         return discordEvent;
    }

    public Message getDiscordMessage()
    {
        if (!messageType.equals(EndPointType.DISCORD))
            throw new IllegalStateException("Attempted to get Discord message from a non-Discord message");
        return discordMessage;
    }

    public void setDiscordMessage(Message discordMessage)
    {
        String parsedMessage = discordMessage.getMessage();
        Pattern userPattern = Pattern.compile("(?<=<@)[0-9]{18}(?=>)");
        Pattern groupPattern = Pattern.compile("(?<=<#)[0-9]{18}(?=>)");

        Matcher userMatcher = userPattern.matcher(parsedMessage);
        while (userMatcher.find())
        {
            String userId = userMatcher.group();
            me.itsghost.jdiscord.talkable.User user = Bot.getAPI().getUserById(userId);
            if (user != null)
                parsedMessage = parsedMessage.replace("<@" + userId + ">", user.getUsername());
        }

        Matcher groupMatcher = groupPattern.matcher(parsedMessage);
        while(groupMatcher.find())
        {
            String groupId = groupMatcher.group();
            Group group = Bot.getAPI().getGroupById(groupId);
            if (group != null)
                parsedMessage = parsedMessage.replace("<#" + groupId + ">", group.getName());
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
