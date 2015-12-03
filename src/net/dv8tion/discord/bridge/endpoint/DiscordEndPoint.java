package net.dv8tion.discord.bridge.endpoint;

public class DiscordEndPoint extends EndPoint
{

    public DiscordEndPoint(EndPointInfo info)
    {
        super(EndPointType.DISCORD);
    }

    public String getServerId()
    {
        return null;
    }

    public String getChannelId()
    {
        return null;
    }

    @Override
    public EndPointInfo toEndPointInfo()
    {
        return null;
    }

}
