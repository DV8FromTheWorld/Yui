package net.dv8tion.discord.bridge.endpoint.types;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.talkable.Group;
import net.dv8tion.discord.Bot;
import net.dv8tion.discord.bridge.endpoint.EndPoint;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointMessage;
import net.dv8tion.discord.bridge.endpoint.EndPointType;

public class DiscordEndPoint extends EndPoint
{
    public static final int MAX_MESSAGE_LENGTH = 2000;

    private String serverId;
    private String groupId;

    public DiscordEndPoint(EndPointInfo info)
    {
        super(EndPointType.DISCORD);
        this.serverId = info.getConnectorId();
        this.groupId = info.getChannelId();
    }

    public String getServerId()
    {
        return serverId;
    }

    public Server getServer()
    {
        return Bot.getAPI().getServerById(serverId);
    }

    public String getGroupId()
    {
        return groupId;
    }

    public Group getGroup()
    {
        return Bot.getAPI().getGroupById(groupId);
    }

    @Override
    public EndPointInfo toEndPointInfo()
    {
        return new EndPointInfo( this.connectionType, this.serverId, this.groupId);
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
        getGroup().sendMessage(message);
    }

    @Override
    public void sendMessage(EndPointMessage message)
    {
        if (!connected)
            throw new IllegalStateException("Cannot send message to disconnected EndPoint! EndPoint: " + this.toEndPointInfo().toString());
        switch (message.getType())
        {
            case DISCORD:
                getGroup().sendMessage(message.getDiscordMessage());
                break;
            default:
                for (String segment : this.divideMessageForSending(message.getMessage()))
                    sendMessage(String.format("<%s> %s", message.getSenderName(), segment));
        }
    }
}
