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

import java.util.List;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class IrcConnectInfo
{
    private String identifier;
    private String host;
    private int port;
    private String nick;
    private String identNick;
    private String identPass;
    private List<String> autojoinChannels;

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getNick()
    {
        return nick;
    }

    public void setNick(String nick)
    {
        this.nick = nick;
    }

    public String getIdentNick()
    {
        return identNick;
    }

    public void setIdentNick(String identNick)
    {
        this.identNick = identNick;
    }

    public String getIdentPass()
    {
        return identPass;
    }

    public void setIdentPass(String identPass)
    {
        this.identPass = identPass;
    }

    public List<String> getAutojoinChannels()
    {
        return autojoinChannels;
    }

    public void setAutojoinChannels(List<String> autojoinChannels)
    {
        this.autojoinChannels = autojoinChannels;
    }

    public Configuration.Builder<PircBotX> getIrcConfigBuilder()
    {
        Configuration.Builder<PircBotX> builder = new Configuration.Builder<PircBotX>();
        builder.setName(nick);
        builder.setServer(host, port);
        builder.setAutoNickChange(true);
        for (String channel : autojoinChannels)
        {
            builder.addAutoJoinChannel(channel);
        }
        return builder;
    }
}

