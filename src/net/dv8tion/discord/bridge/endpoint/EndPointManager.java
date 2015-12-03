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
        return null;
    }
}
