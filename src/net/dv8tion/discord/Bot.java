package net.dv8tion.discord;

import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.DiscordBuilder;
import me.itsghost.jdiscord.event.EventManager;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;
import net.dv8tion.discord.commands.AnimeNewsNetworkCommand;
import net.dv8tion.discord.commands.MyAnimeListCommand;
import net.dv8tion.discord.commands.NyaaCommand;
import net.dv8tion.discord.commands.SearchCommand;
import net.dv8tion.discord.commands.TestCommand;

public class Bot
{

    public static void main(String[] args)
    {
        try
        {
            Settings settings = SettingsManager.getInstance().getSettings();
            DiscordAPI api = new DiscordBuilder(settings.getEmail(), settings.getPassword()).build().login();

            EventManager manager = api.getEventManager();
            manager.registerListener(new TestCommand());
            manager.registerListener(new SearchCommand());
            manager.registerListener(new NyaaCommand());
            manager.registerListener(new MyAnimeListCommand());
            manager.registerListener(new AnimeNewsNetworkCommand());
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
