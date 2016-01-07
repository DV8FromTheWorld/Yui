package net.dv8tion.discord.bridge;

import net.dv8tion.discord.SettingsManager;
import net.dv8tion.discord.bridge.endpoint.EndPoint;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointManager;


public class BridgeManager
{
    private static BridgeManager instance;
    private SettingsManager settingsManager;

    private BridgeManager()
    {
        settingsManager = SettingsManager.getInstance();
    }

    public static BridgeManager getInstance()
    {
        if (instance == null)
            instance = new BridgeManager();
        return instance;
    }

    public EndPoint getOtherEndPoint(EndPointInfo info)
    {
        //We get the list (getBridges) every time instead of storing it because the settings could have been reloaded or a new bridge added.
        for (BridgeInfo bridgeInfo : settingsManager.getSettings().getBridges())
        {
            //If this bridge doesn't have the EndPoint we are working with, move on.
            if (!bridgeInfo.contains(info))
                continue;
            if (bridgeInfo.getEndPoint1().equals(info))
            {
                //We were given point1 and asked for the other, so give point2
                return EndPointManager.getInstance().getEndPoint(bridgeInfo.getEndPoint2());
            }
            else
            {
                //We were given point2 and asked for the other, so give point1
                return EndPointManager.getInstance().getEndPoint(bridgeInfo.getEndPoint1());
            }
        }
        return null;
    }
}
