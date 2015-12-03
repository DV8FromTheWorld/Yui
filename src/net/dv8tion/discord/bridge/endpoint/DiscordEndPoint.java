package net.dv8tion.discord.bridge.endpoint;

public class DiscordEndPoint extends EndPoint
{
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

    public String getGroupId()
    {
        return groupId;
    }

    @Override
    public EndPointInfo toEndPointInfo()
    {
        return new EndPointInfo(this.serverId, this.groupId, this.connectionType);
    }

}
