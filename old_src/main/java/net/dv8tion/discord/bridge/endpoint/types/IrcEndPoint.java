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
import org.pircbotx.Channel;

public class IrcEndPoint extends EndPoint
{
    public static final int MAX_LINE_LENGTH = 450;
    public static final char NAME_BREAK_CHAR = '\u200B';

    private String connectionName;
    private String channelName;
    private Channel channel;

    public IrcEndPoint(EndPointInfo info)
    {
        super(EndPointType.IRC);
        this.connectionName = info.getConnectorId();
        this.channelName = info.getChannelId();
    }

    public Channel getChannel()
    {
        if (channel != null)
            return channel;
        for (Channel c : Yui.getIrcConnection(connectionName).getIrcBot().getUserBot().getChannels())
        {
            if (c.getName().equals(channelName))
            {
                channel = c;
                return c;
            }
        }
        return null;
    }

    public String getChannelName()
    {
        return channelName;
    }

    @Override
    public EndPointInfo toEndPointInfo()
    {
        return new EndPointInfo(connectionType, connectionName, channelName);
    }


    @Override
    public int getMaxMessageLength()
    {
        return MAX_LINE_LENGTH;
    }

    @Override
    public void sendMessage(String message)
    {
        if (!connected)
            throw new IllegalStateException("Cannot send message to disconnected EndPoint! EndPoint: " + this.toEndPointInfo().toString());
        this.getChannel().send().message(message);
    }

    @Override
    public void sendMessage(EndPointMessage message)
    {
        if (!connected)
            throw new IllegalStateException("Cannot send message to disconnected EndPoint! EndPoint: " + this.toEndPointInfo().toString());
        String[] lines = message.getMessage().split("\n");
        for (String line : lines)
        {
            for (String segment : this.divideMessageForSending(line))
            {
                String username = message.getSenderName();
                StringBuilder builder = new StringBuilder();
                builder.append("<");
                if (username.length() > 1)
                {
                    int midway = username.length() / 2;
                    builder.append(username.substring(0, midway));
                    builder.append(NAME_BREAK_CHAR);
                    builder.append(username.substring(midway));
                }
                else
                    builder.append(username);
                builder.append("> ");
                builder.append(segment);
                this.sendMessage(builder.toString());
            }
        }
    }
}
