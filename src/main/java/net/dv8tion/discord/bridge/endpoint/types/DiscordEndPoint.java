package net.dv8tion.discord.bridge.endpoint.types;

import net.dv8tion.discord.Yui;
import net.dv8tion.discord.bridge.endpoint.EndPoint;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointMessage;
import net.dv8tion.discord.bridge.endpoint.EndPointType;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;

public class DiscordEndPoint extends EndPoint
{
    public static final int MAX_MESSAGE_LENGTH = 2000;

    private String guildId;
    private String channelId;

    public DiscordEndPoint(EndPointInfo info)
    {
        super(EndPointType.DISCORD);
        this.guildId = info.getConnectorId();
        this.channelId = info.getChannelId();
    }

    public String getGuildId()
    {
        return guildId;
    }

    public Guild getGuild()
    {
        return Yui.getAPI().getGuildById(guildId);
    }

    public String getChannelId()
    {
        return channelId;
    }

    public TextChannel getChannel()
    {
        return Yui.getAPI().getTextChannelById(channelId);
    }

    @Override
    public EndPointInfo toEndPointInfo()
    {
        return new EndPointInfo( this.connectionType, this.guildId, this.channelId);
    }

    @Override
    public int getMaxMessageLength()
    {
        return MAX_MESSAGE_LENGTH;
    }

    @Override
    public void sendMessage(String message)
    {
        if (!connected)
            throw new IllegalStateException("Cannot send message to disconnected EndPoint! EndPoint: " + this.toEndPointInfo().toString());
        getChannel().sendMessage(message);
    }

    @Override
    public void sendMessage(EndPointMessage message)
    {
        if (!connected)
            throw new IllegalStateException("Cannot send message to disconnected EndPoint! EndPoint: " + this.toEndPointInfo().toString());
        switch (message.getType())
        {
            case DISCORD:
                getChannel().sendMessage(message.getDiscordMessage());
                break;
            default:
                for (String segment : this.divideMessageForSending(message.getMessage()))
                    sendMessage(String.format("<%s> %s", message.getSenderName(), segment));
        }
    }
}
