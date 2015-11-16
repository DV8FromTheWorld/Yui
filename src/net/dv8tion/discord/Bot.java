package net.dv8tion.discord;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.DiscordBuilder;
import me.itsghost.jdiscord.event.EventManager;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;
import net.dv8tion.discord.commands.AnimeNewsNetworkCommand;
import net.dv8tion.discord.commands.MyAnimeListCommand;
import net.dv8tion.discord.commands.NyaaCommand;
import net.dv8tion.discord.commands.ReloadCommand;
import net.dv8tion.discord.commands.SearchCommand;
import net.dv8tion.discord.commands.TestCommand;

public class Bot
{

    public static void main(String[] args) throws IOException, InterruptedException
    {
        if (System.getProperty("file.encoding").equals("UTF-8"))
        {
            setupBot();
        }
        else
        {
            System.out.println("BotLauncher: We are not running in UTF-8 mode! This is a problem!");
            System.out.println("BotLauncher: Relaunching in UTF-8 mode using -Dfile.encoding=UTF-8");

            //Gets the path of the currently running Jar file
            String path = Bot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            File f = new File(decodedPath); //We use File so that when we send the path to the ProcessBuilder, we will be using the proper System path formatting.

            //Relaunches the bot using UTF-8 mode.
            ProcessBuilder processBuilder =
                    new ProcessBuilder("java", "-Dfile.encoding=UTF-8", "-jar", f.getAbsolutePath());
            processBuilder.inheritIO(); //Tells the new process to use the same command line as this one.
            Process process = processBuilder.start();
            process.waitFor();  //We wait here until the actual bot stops. We do this so that we can keep using the same command line.
        }
    }

    private static void setupBot()
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
            manager.registerListener(new ReloadCommand(api));
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
