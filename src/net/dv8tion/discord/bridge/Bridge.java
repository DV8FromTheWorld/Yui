package net.dv8tion.discord.bridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dv8tion.discord.bridge.endpoint.EndPoint;
import net.dv8tion.discord.bridge.endpoint.EndPointType;

public class Bridge
{
    private EndPoint point1;
    private EndPoint point2;

    public Bridge(EndPoint point1, EndPoint point2)
    {
        this.point1 = point1;
        this.point2 = point2;
    }

    public List<EndPoint> getEndPoints()
    {
        return Arrays.asList(point1, point2);
    }

    public List<EndPoint> getEndPoints(EndPointType type)
    {
        List<EndPoint> list = new ArrayList<EndPoint>();
        if (point1.isType(type)) list.add(point1);
        if (point2.isType(type)) list.add(point2);
        return list;
    }
}
