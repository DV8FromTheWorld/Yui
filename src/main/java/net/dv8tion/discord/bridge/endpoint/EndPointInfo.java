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

import net.dv8tion.jda.core.entities.TextChannel;
import org.pircbotx.Channel;

public class EndPointInfo
{
    public static final String SEPARATOR = ":";
    private EndPointType type;
    private String connectorId;
    private String channelId;

    public EndPointInfo(EndPointType type, String connectorId, String channelId)
    {
        this.connectorId = connectorId;
        this.channelId = channelId;
        this.type = type;
    }

    public String getConnectorId()
    {
        return connectorId;
    }

    public void setConnectorId(String connectorId)
    {
        this.connectorId = connectorId;
    }

    public String getChannelId()
    {
        return channelId;
    }

    public void setChannelId(String channelId)
    {
        this.channelId = channelId;
    }

    public String toString()
    {
        return type.getName() + SEPARATOR + connectorId + SEPARATOR + channelId;
    }

    public EndPointType getType()
    {
        return type;
    }

    public void setType(EndPointType type)
    {
        this.type = type;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof EndPointInfo))
            return false;
        EndPointInfo oInfo = (EndPointInfo) o;
        return toString().equals(oInfo.toString());
    }

    public static EndPointInfo createFromDiscordChannel(TextChannel channel)
    {
        return new EndPointInfo(EndPointType.DISCORD, channel.getGuild().getId(), channel.getId());
    }

    public static EndPointInfo createFromIrcChannel(String identifier, Channel channel)
    {
        return new EndPointInfo(EndPointType.IRC, identifier, channel.getName());
    }
}
