package net.dv8tion.discord.bridge.endpoint;

import org.pircbotx.Channel;

public class IrcEndPoint extends EndPoint
{
    public IrcEndPoint(EndPointInfo info)
    {
        super(info, EndPointType.IRC);
    }

    public Channel getChannel()
    {
        return null;
    }

    public String getChannelName()
    {
        return null;
    }
}
