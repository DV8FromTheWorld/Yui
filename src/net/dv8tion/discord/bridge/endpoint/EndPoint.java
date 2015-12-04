package net.dv8tion.discord.bridge.endpoint;

public abstract class EndPoint
{
    protected EndPointType connectionType;

    public abstract EndPointInfo toEndPointInfo();

    protected EndPoint(EndPointType connectionType)
    {
        this.connectionType = connectionType;
    }

    public boolean isType(EndPointType connectionType)
    {
        return this.connectionType.equals(connectionType);
    }
}
