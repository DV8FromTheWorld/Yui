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
package net.dv8tion.discord.bridge.endpoint.types;

import net.dv8tion.discord.Yui;
import net.dv8tion.discord.bridge.endpoint.EndPoint;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointMessage;
import net.dv8tion.discord.bridge.endpoint.EndPointType;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;

public class DiscordEndPoint extends EndPoint
{
    public static final int MAX_MESSAGE_LENGTH = 2000;

    private String guildId;
    private String channelId;

    public DiscordEndPoint(EndPointInfo info)
    {
        super(EndPointType.DISCORD);
        this.guildId = info.getConnectorId();
        this.channelId = info.getChannelId();
    }

    public String getGuildId()
    {
        return guildId;
    }

    public Guild getGuild()
    {
        return Yui.getAPI().getGuildById(guildId);
    }

    public String getChannelId()
    {
        return channelId;
    }

    public TextChannel getChannel()
    {
        return Yui.getAPI().getTextChannelById(channelId);
    }

    @Override
    public EndPointInfo toEndPointInfo()
    {
        return new EndPointInfo( this.connectionType, this.guildId, this.channelId);
    }

    @Override
    public int getMaxMessageLength()
    {
        return MAX_MESSAGE_LENGTH;
    }

    @Override
    public void sendMessage(String message)
    {
        if (!connected)
            throw new IllegalStateException("Cannot send message to disconnected EndPoint! EndPoint: " + this.toEndPointInfo().toString());
        getChannel().sendMessage(message);
    }

    @Override
    public void sendMessage(EndPointMessage message)
    {
        if (!connected)
            throw new IllegalStateException("Cannot send message to disconnected EndPoint! EndPoint: " + this.toEndPointInfo().toString());
        switch (message.getType())
        {
            case DISCORD:
                getChannel().sendMessage(message.getDiscordMessage());
                break;
            default:
                for (String segment : this.divideMessageForSending(message.getMessage()))
                    sendMessage(String.format("<%s> %s", message.getSenderName(), segment));
        }
    }
}
