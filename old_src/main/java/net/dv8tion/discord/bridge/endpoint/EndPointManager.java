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
package net.dv8tion.discord.bridge.endpoint;

import net.dv8tion.discord.bridge.endpoint.types.DiscordEndPoint;
import net.dv8tion.discord.bridge.endpoint.types.IrcEndPoint;

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
