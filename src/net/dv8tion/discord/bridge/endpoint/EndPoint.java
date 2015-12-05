package net.dv8tion.discord.bridge.endpoint;

public abstract class EndPoint
{
    protected EndPointType connectionType;
    protected boolean connected;

    public abstract EndPointInfo toEndPointInfo();

    protected EndPoint(EndPointType connectionType)
    {
        this.connectionType = connectionType;
        connected = true;
    }

    public boolean isConnected()
    {
        return connected;
    }

    protected void setConnected(boolean connected)
    {
        this.connected = connected;
    }

    public boolean isType(EndPointType connectionType)
    {
        return this.connectionType.equals(connectionType);
    }
}
