/**
 * This code came from Smbarbour's RavenBot
 * https://github.com/MCUpdater/RavenBot/blob/master/src/main/java/org/mcupdater/ravenbot/Settings.java
 */

package net.dv8tion.discord;

public class Settings {
    private String email;
    private String password;
    private String githubRepoUrl;

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

    public String getGithubRepoUrl()
    {
        return githubRepoUrl;
    }

    public void setGithubRepoUrl(String gitRepoUrl)
    {
        this.githubRepoUrl = gitRepoUrl;
    }
}
