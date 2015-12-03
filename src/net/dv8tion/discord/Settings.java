/**
 * This code came from Smbarbour's RavenBot
 * https://github.com/MCUpdater/RavenBot/blob/master/src/main/java/org/mcupdater/ravenbot/Settings.java
 */

package net.dv8tion.discord;

import java.util.List;
import java.util.Map;

import net.dv8tion.discord.bridge.IRCConnectInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;

public class Settings {
    private String email;
    private String password;
    private List<IRCConnectInfo> ircConnectInfos;
    private Map<EndPointInfo, EndPointInfo> bridges;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<IRCConnectInfo> getIrcConnectInfos()
    {
        return ircConnectInfos;
    }

    public void setIrcConnectInfos(List<IRCConnectInfo> ircConnectInfos)
    {
        this.ircConnectInfos = ircConnectInfos;
    }

    public Map<EndPointInfo, EndPointInfo> getBridges()
    {
        return bridges;
    }

    public void setBridges(Map<EndPointInfo, EndPointInfo> bridges)
    {
        this.bridges = bridges;
    }
}
