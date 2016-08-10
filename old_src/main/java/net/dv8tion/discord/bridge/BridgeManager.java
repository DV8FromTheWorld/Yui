/**
 *     Copyright 2015-2016 Austin Keener
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
