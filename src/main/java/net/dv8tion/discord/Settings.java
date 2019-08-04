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
package net.dv8tion.discord;

import net.dv8tion.discord.bridge.BridgeInfo;
import net.dv8tion.discord.bridge.IrcConnectInfo;

import java.util.List;

/**
 * This code came from Smbarbour's RavenBot
 * https://github.com/MCUpdater/RavenBot/blob/master/src/main/java/org/mcupdater/ravenbot/Settings.java
 */
public class Settings {
    private String botToken;
    private String googleApiKey;
    private String proxyHost;
    private String proxyPort;
    private List<IrcConnectInfo> ircConnectInfos;
    private List<BridgeInfo> bridges;

    public String getBotToken()
    {
        return botToken;
    }

    public void setBotToken(String botToken)
    {
        this.botToken = botToken;
    }

    public String getGoogleApiKey()
    {
        return googleApiKey;
    }

    public void setGoogleApiKey(String googleApiKey)
    {
        this.googleApiKey = googleApiKey;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort)
    {
        this.proxyPort = proxyPort;
    }

    public List<IrcConnectInfo> getIrcConnectInfos()
    {
        return ircConnectInfos;
    }

    public void setIrcConnectInfos(List<IrcConnectInfo> ircConnectInfos)
    {
        this.ircConnectInfos = ircConnectInfos;
    }

    public List<BridgeInfo> getBridges()
    {
        return bridges;
    }

    public void setBridges(List<BridgeInfo> bridges)
    {
        this.bridges = bridges;
    }
}
