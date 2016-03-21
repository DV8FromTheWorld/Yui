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

    public boolean contains(EndPointInfo endPointInfo)
    {
        return endPoint1.equals(endPointInfo) || endPoint2.equals(endPointInfo);
    }
}
