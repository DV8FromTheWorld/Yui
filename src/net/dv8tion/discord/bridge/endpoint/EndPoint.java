package net.dv8tion.discord.bridge.endpoint;

public abstract class EndPoint
{
    protected EndPointInfo info;
    protected EndPointType connectionType;

    public abstract boolean isType(EndPointInfo info);
    public abstract EndPointInfo toEndPointInfo();

    public enum EndPointType
    {
        DISCORD("DISCORD"),
        IRC("IRC"),
        ANY("ANY");

        private String name;
        EndPointType(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

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
