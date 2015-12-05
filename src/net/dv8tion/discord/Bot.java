package net.dv8tion.discord;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.DiscordBuilder;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.event.EventManager;
import me.itsghost.jdiscord.events.APILoadedEvent;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;
import me.itsghost.jdiscord.talkable.Group;
import net.dv8tion.discord.bridge.IRCConnectInfo;
import net.dv8tion.discord.bridge.IrcConnection;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointManager;
import net.dv8tion.discord.commands.AnimeNewsNetworkCommand;
import net.dv8tion.discord.commands.HelpCommand;
import net.dv8tion.discord.commands.MyAnimeListCommand;
import net.dv8tion.discord.commands.NyaaCommand;
import net.dv8tion.discord.commands.PermissionsCommand;
import net.dv8tion.discord.commands.ReloadCommand;
import net.dv8tion.discord.commands.SearchCommand;
import net.dv8tion.discord.commands.TestCommand;
import net.dv8tion.discord.commands.UpdateCommand;
import net.dv8tion.discord.fixes.EventManagerX;
import net.dv8tion.discord.util.Database;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class Bot
{
    public static final int NORMAL_SHUTDOWN = 10;
    public static final int RESTART_EXITCODE = 19;
    public static final int UPDATE_EXITCODE = 20;
    public static final int NEWLY_CREATED_CONFIG = 21;
    public static final int UNABLE_TO_CONNECT_TO_DISCORD = 22;
    public static final int BAD_USERNAME_PASS_COMBO = 23;
    public static final int NO_USERNAME_PASS_COMBO = 24;

    public static final String LATEST_BUILD_DATE_URL = "https://drone.io/github.com/DV8FromTheWorld/Discord-Bot/files/target/classes/build-date.txt";
    public static final String BUILD_DATE_FILE_NAME = "build-date.txt";
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    private static Date BUILD_DATE;
    private static DiscordAPI api;

    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException
    {
        if (System.getProperty("file.encoding").equals("UTF-8"))
        {
            setupBot();
        }
        else
        {
            relaunchInUTF8();
        }
    }

    public static File getThisJarFile() throws UnsupportedEncodingException
    {
      //Gets the path of the currently running Jar file
        String path = Bot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(path, "UTF-8");

        //This is code especially written for running and testing this program in an IDE that doesn't compile to .jar when running.
        if (!decodedPath.endsWith(".jar"))
        {
            return new File("Yui.jar");
        }
        return new File(decodedPath);   //We use File so that when we send the path to the ProcessBuilder, we will be using the proper System path formatting.
    }

    public static Date getBuildDate()
    {
        return (Date) BUILD_DATE.clone();
    }

    public static DiscordAPI getAPI()
    {
        return api;
    }

    private static void setupBot()
    {
        try
        {
            Settings settings = SettingsManager.getInstance().getSettings();
            try
            {
                //Used when in IDE
                File buildDateFile = new File(BUILD_DATE_FILE_NAME);
                if (buildDateFile.exists())
                {
                    String date = new String(Files.readAllBytes(Paths.get(BUILD_DATE_FILE_NAME)), "UTF-8");
                    BUILD_DATE = DATE_FORMATTER.parse(date);
                }
                else    //Used when in JAR.
                {
                    String date = new String(
                            IOUtils.toByteArray(
                                    Thread.currentThread().getContextClassLoader().getResourceAsStream(BUILD_DATE_FILE_NAME)),
                            "UTF-8");
                    BUILD_DATE = DATE_FORMATTER.parse(date);
                }
            }
            catch (IOException | ParseException e)
            {
                BUILD_DATE = null;
                System.out.println("Could not determine build date.");
                e.printStackTrace();
            }
            api = new DiscordBuilder(settings.getEmail(), settings.getPassword()).build().login();
            EventManagerX.replaceEventManager(api); //TODO: Remove this once jDiscord includes the fix.
            Database.getInstance();
            Permissions.setupPermissions();

            EventManager manager = api.getEventManager();
            HelpCommand help = new HelpCommand();
            manager.registerListener(help.registerCommand(help));
            manager.registerListener(help.registerCommand(new TestCommand()));
            manager.registerListener(help.registerCommand(new SearchCommand()));
            manager.registerListener(help.registerCommand(new NyaaCommand()));
            manager.registerListener(help.registerCommand(new MyAnimeListCommand()));
            manager.registerListener(help.registerCommand(new AnimeNewsNetworkCommand()));
            manager.registerListener(help.registerCommand(new ReloadCommand()));
            manager.registerListener(help.registerCommand(new UpdateCommand()));
            manager.registerListener(help.registerCommand(new PermissionsCommand()));
            for (IRCConnectInfo info  : settings.getIrcConnectInfos())
            {
                manager.registerListener(new IrcConnection(info));
            }

            manager.registerListener(new EventListener()
            {
                @SuppressWarnings("unused")
                public void onApiLoaded(APILoadedEvent e)
                {
                    //Creates and Stores all Discord endpoints in our Manager.
                    for (Server server : api.getAvailableServers())
                    {
                        for (Group group : server.getGroups())
                        {
                            EndPointManager.getInstance().createEndPoint(EndPointInfo.createFromDiscordGroup(group));
                        }
                    }
                    Permissions.getPermissions().setBotAsOp(api.getSelfInfo());
                }
            });
        }
        catch (NoLoginDetailsException e)
        {
            System.out.println("No login details provided! Please give an email and password in the config file.");
            System.exit(NO_USERNAME_PASS_COMBO);
        }
        catch (BadUsernamePasswordException e)
        {
            System.out.println("The Email and Password combination provided in the Config.json was incorrect.");
            System.out.println("Did you modify the Config.json after it was created?");
            System.exit(BAD_USERNAME_PASS_COMBO);
        }
        catch (DiscordFailedToConnectException e)
        {
            System.out.println("We failed to connect to the Discord API. Do you have internet connection?");
            System.out.println("Also double-check your Config.json for possible mistakes.");
            System.exit(UNABLE_TO_CONNECT_TO_DISCORD);
        }
    }

    private static void relaunchInUTF8() throws InterruptedException, UnsupportedEncodingException
    {
        System.out.println("BotLauncher: We are not running in UTF-8 mode! This is a problem!");
        System.out.println("BotLauncher: Relaunching in UTF-8 mode using -Dfile.encoding=UTF-8");

        String[] command = new String[] {"java", "-Dfile.encoding=UTF-8", "-jar", Bot.getThisJarFile().getAbsolutePath()};

        //Relaunches the bot using UTF-8 mode.
        ProcessBuilder processBuilder =  new ProcessBuilder(command);
        processBuilder.inheritIO(); //Tells the new process to use the same command line as this one.
        try
        {
            Process process = processBuilder.start();
            process.waitFor();  //We wait here until the actual bot stops. We do this so that we can keep using the same command line.
            System.exit(process.exitValue());
        }
        catch (IOException e)
        {
            if (e.getMessage().contains("\"java\""))
            {
                System.out.println("BotLauncher: There was an error relaunching the bot. We couldn't find Java to launch with.");
                System.out.println("BotLauncher: Attempted to relaunch using the command:\n   " + StringUtils.join(command, " ", 0, command.length));
                System.out.println("BotLauncher: Make sure that you have Java properly set in your Operating System's PATH variable.");
                System.out.println("BotLauncher: Stopping here.");
            }
            else
            {
                e.printStackTrace();
            }
        }
    }
}
