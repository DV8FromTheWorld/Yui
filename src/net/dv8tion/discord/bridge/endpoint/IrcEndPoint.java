package net.dv8tion.discord.bridge.endpoint;

import org.pircbotx.Channel;

public class IrcEndPoint extends EndPoint
{
    private String connectionName;
    private String channelName;

    public IrcEndPoint(EndPointInfo info)
    {
        super(EndPointType.IRC);
        this.connectionName = info.getConnectorId();
        this.channelName = info.getChannelId();
    }

    public Channel getChannel()
    {
        //TODO: get IRC channel.
        return null;
    }

    public String getChannelName()
    {
        return channelName;
    }

    @Override
    public EndPointInfo toEndPointInfo()
    {
        return new EndPointInfo(connectionName, channelName, connectionType);
    }
}
