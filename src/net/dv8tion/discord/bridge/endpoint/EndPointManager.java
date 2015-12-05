package net.dv8tion.discord.bridge.endpoint;

import java.util.ArrayList;
import java.util.List;

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

    public EndPoint getOrCreate(EndPointInfo info)
    {
        EndPoint endPoint = null;
        switch (info.getType())
        {
            case DISCORD:
                endPoint = getEndPointFromInfo(info);
                if (endPoint != null)
                    return endPoint;
                else
                {
                    EndPoint newEndPoint = new DiscordEndPoint(info);
                    endPoints.add(newEndPoint);
                    return newEndPoint;
                }
            case IRC:
                endPoint = getEndPointFromInfo(info);
                if (endPoint != null)
                    return endPoint;
                else
                {
                    EndPoint newEndPoint = new IrcEndPoint(info);
                    endPoints.add(newEndPoint);
                    return newEndPoint;
                }
            case UNKNOWN:
                //FAIL
                return null;
            default:
                throw new RuntimeException("We were provided an unknown EndPointType: " + info.getType().getName());
        }
    }

    public List<EndPoint> getEndPoints()
    {
        return endPoints;
    }

    public void informDisconnect(EndPointInfo endPointInfo)
    {
        //TODO: Consider informing bridges that the EndPoint has disconnected.
        EndPoint endPoint = getEndPointFromInfo(endPointInfo);
        if (endPoint != null)
            endPoint.setConnected(false);
        else
            throw new RuntimeException("Tried to inform disconnect on EndPoint but could not find EndPoint in Manager. EndPoint: " + endPointInfo.toString());
    }

    public void informReconnect(EndPointInfo endPointInfo)
    {
      //TODO: Consider informing bridges that the EndPoint has reconnected.
        EndPoint endPoint = getEndPointFromInfo(endPointInfo);
        if (endPoint != null)
            endPoint.setConnected(true);
        else
            throw new RuntimeException("Tried to inform reconnect on EndPoint but could not find EndPoint in Manager. EndPoint: " + endPointInfo.toString());
    }

    private EndPoint getEndPointFromInfo(EndPointInfo info)
    {
        for (EndPoint point : endPoints)
        {
            if (point.toEndPointInfo().equals(info))
                return point;
        }
        return null;
    }
}
