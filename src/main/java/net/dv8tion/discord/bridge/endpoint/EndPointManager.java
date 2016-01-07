package net.dv8tion.discord.bridge.endpoint;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.discord.bridge.endpoint.types.DiscordEndPoint;
import net.dv8tion.discord.bridge.endpoint.types.IrcEndPoint;

public class EndPointManager
{
    private static EndPointManager manager;
    private List<EndPoint> endPoints;

    private EndPointManager()
    {
        endPoints = new ArrayList<EndPoint>();
    }

    public static EndPointManager getInstance()
    {
        if (manager == null)
            manager = new EndPointManager();
        return manager;
    }

    public EndPoint createEndPoint(EndPointInfo info)
    {
        if (getEndPoint(info) != null)
            throw new RuntimeException("We tried to create an EndPoint but it already existed! EndPointInfo: " + info.toString());

        switch (info.getType())
        {
            case DISCORD:
                EndPoint discordEndPoint = new DiscordEndPoint(info);
                endPoints.add(discordEndPoint);
                return discordEndPoint;
            case IRC:
                EndPoint ircEndPoint = new IrcEndPoint(info);
                endPoints.add(ircEndPoint);
                return ircEndPoint;
            case UNKNOWN:
                throw new RuntimeException("You can't make an endpoint from type UNKNOWN! EndPointInfo: " + info.toString());
            default:
                throw new RuntimeException("We were provided an unrecognized EndPointType. EndPointInfo: " + info.toString());
        }
    }

    public EndPoint getEndPoint(EndPointInfo info)
    {
        for (EndPoint point : endPoints)
        {
            if (point.toEndPointInfo().equals(info))
                return point;
        }
        return null;
    }

    public List<EndPoint> getEndPoints()
    {
        return endPoints;
    }

    public void informDisconnect(EndPointInfo endPointInfo)
    {
        //TODO: Consider informing bridges that the EndPoint has disconnected.
        EndPoint endPoint = getEndPoint(endPointInfo);
        if (endPoint != null)
            endPoint.setConnected(false);
        else
            throw new RuntimeException("Tried to inform disconnect on EndPoint but could not find EndPoint in Manager. EndPoint: " + endPointInfo.toString());
    }

    public void informReconnect(EndPointInfo endPointInfo)
    {
      //TODO: Consider informing bridges that the EndPoint has reconnected.
        EndPoint endPoint = getEndPoint(endPointInfo);
        if (endPoint != null)
            endPoint.setConnected(true);
        else
            throw new RuntimeException("Tried to inform reconnect on EndPoint but could not find EndPoint in Manager. EndPoint: " + endPointInfo.toString());
    }
}
