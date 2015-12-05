package net.dv8tion.discord.bridge;

import net.dv8tion.discord.bridge.endpoint.EndPointInfo;

public class BridgeInfo
{
    private EndPointInfo endPoint1;
    private EndPointInfo endPoint2;

    public static BridgeInfo create(EndPointInfo endPoint1, EndPointInfo endPoint2)
    {
        BridgeInfo info = new BridgeInfo();
        info.setEndPoint1(endPoint1);
        info.setEndPoint2(endPoint2);
        return info;
    }
    public EndPointInfo getEndPoint1()
    {
        return endPoint1;
    }

    public void setEndPoint1(EndPointInfo endPoint1)
    {
        this.endPoint1 = endPoint1;
    }

    public EndPointInfo getEndPoint2()
    {
        return endPoint2;
    }

    public void setEndPoint2(EndPointInfo endPoint2)
    {
        this.endPoint2 = endPoint2;
    }
}
