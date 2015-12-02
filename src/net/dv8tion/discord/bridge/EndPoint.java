package net.dv8tion.discord.bridge;

public class EndPoint
{
    public static final String SEPARATOR = ":";
    private String connectorId;
    private String channelId;

    public EndPoint() {};
    public EndPoint(String connectorId, String channelId)
    {
        this.connectorId = connectorId;
        this.channelId = channelId;
    }

    public String getConnectorId()
    {
        return connectorId;
    }

    public void setConnectorId(String connectorId)
    {
        this.connectorId = connectorId;
    }

    public String getChannelId()
    {
        return channelId;
    }

    public void setChannelId(String channelId)
    {
        this.channelId = channelId;
    }

    public String toString()
    {
        return connectorId + SEPARATOR + channelId;
    }

    public static EndPoint getDefault()
    {
        return new EndPoint("", "");
    }
}
