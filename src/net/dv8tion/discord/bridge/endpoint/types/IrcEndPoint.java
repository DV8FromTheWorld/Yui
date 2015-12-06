package net.dv8tion.discord.bridge.endpoint.types;

import net.dv8tion.discord.Bot;
import net.dv8tion.discord.bridge.endpoint.EndPoint;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointMessage;
import net.dv8tion.discord.bridge.endpoint.EndPointType;

import org.pircbotx.Channel;

public class IrcEndPoint extends EndPoint
{
    private String connectionName;
    private String channelName;
    private Channel channel;

    public IrcEndPoint(EndPointInfo info)
    {
        super(EndPointType.IRC);
        this.connectionName = info.getConnectorId();
        this.channelName = info.getChannelId();
    }

    public Channel getChannel()
    {
        if (channel != null)
            return channel;
        for (Channel c : Bot.getIrcConnection(connectionName).getIrcBot().getUserBot().getChannels())
        {
            if (c.getName().equals(channelName))
            {
                channel = c;
                return c;
            }
        }
        return null;
    }

    public String getChannelName()
    {
        return channelName;
    }

    @Override
    public EndPointInfo toEndPointInfo()
    {
        return new EndPointInfo(connectionType, connectionName, channelName);
    }

    @Override
    public void sendMessage(String message)
    {
        if (!connected)
            throw new IllegalStateException("Cannot send message to disconnected EndPoint! EndPoint: " + this.toEndPointInfo().toString());
        this.getChannel().send().message(message);
    }

    @Override
    public void sendMessage(EndPointMessage message)
    {
        if (!connected)
            throw new IllegalStateException("Cannot send message to disconnected EndPoint! EndPoint: " + this.toEndPointInfo().toString());
        this.sendMessage(String.format("<%s> %s", message.getSenderName(), message.getMessage()));        
    }
}
