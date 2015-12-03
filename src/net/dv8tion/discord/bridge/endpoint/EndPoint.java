package net.dv8tion.discord.bridge.endpoint;

public abstract class EndPoint
{
    public enum EndPointType
    {
        DISCORD, IRC, ANY;
    }

    protected EndPointInfo info;
    protected EndPointType connectionType;

    public EndPoint(EndPointInfo info)
    {
        this(info, EndPointType.ANY);
    }

    protected EndPoint(EndPointInfo info, EndPointType connectionType)
    {
        this.info = info;
        this.connectionType = connectionType;
    }

    public EndPointType getType()
    {
        return connectionType;
    }

    public boolean isType(EndPointType connectionType)
    {
        return this.connectionType.equals(connectionType);
    }

    protected void setType(EndPointType connectionType)
    {
        this.connectionType = connectionType;
    }
}
