package net.dv8tion.discord;

import net.dv8tion.discord.commands.TestCommand;
import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.DiscordBuilder;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;

public class Bot
{

    public static void main(String[] args)
    {
        try
        {
            Settings settings = SettingsManager.getInstance().getSettings();
            DiscordAPI api = new DiscordBuilder(settings.getEmail(), settings.getPassword()).build().login();
            api.getEventManager().registerListener(new TestCommand());
        }
        catch (NoLoginDetailsException e)
        {
            System.out.println("No login details provided! Please give an email and password in the config file.");
        }
        catch (BadUsernamePasswordException e)
        {
            System.out.println("The Email and Password combination provided in the Config.json was incorrect.");
            System.out.println("Did you modify the Config.json after it was created?");
        }
        catch (DiscordFailedToConnectException e)
        {
            System.out.println("We failed to connect to the Discord API. Do you have internet connection?");
            System.out.println("Also double-check your Config.json for possible mistakes.");
        }
    }
}
