package net.dv8tion.discord.bridge.endpoint;

public abstract class EndPoint
{
    protected EndPointType connectionType;

    public abstract EndPointInfo toEndPointInfo();

    public enum EndPointType
    {
        DISCORD("DISCORD"),
        IRC("IRC"),
        UNKNOWN(null);

        private String name;
        EndPointType(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public static EndPointType getFromName(String name)
        {
            for (EndPointType type : values())
            {
                if (type.getName().equals(name))
                    return type;
            }
            return UNKNOWN;
        }
    }

    protected EndPoint(EndPointType connectionType)
    {
        this.connectionType = connectionType;
    }

    public boolean isType(EndPointType connectionType)
    {
        return this.connectionType.equals(connectionType);
    }
}
