/**
 * This code came from Smbarbour's RavenBot
 * https://github.com/MCUpdater/RavenBot/blob/master/src/main/java/org/mcupdater/ravenbot/Settings.java
 */

package net.dv8tion.discord;

import java.util.List;

import net.dv8tion.discord.bridge.BridgeInfo;
import net.dv8tion.discord.bridge.IrcConnectInfo;

public class Settings {
    private String email;
    private String password;
    private Boolean useBetaBuilds;
    private List<IrcConnectInfo> ircConnectInfos;
    private List<BridgeInfo> bridges;

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

    public Boolean getUseBetaBuilds()
    {
        return useBetaBuilds;
    }

    public void setUseBetaBuilds(boolean useBetaBuilds)
    {
        this.useBetaBuilds = useBetaBuilds;
    }
}
