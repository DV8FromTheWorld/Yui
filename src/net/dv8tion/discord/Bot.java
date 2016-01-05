package net.dv8tion.discord;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.dv8tion.discord.bridge.IrcConnectInfo;
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
import net.dv8tion.discord.util.Database;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;

import javax.security.auth.login.LoginException;

public class Bot
{
    //Non error, no action exit codes.
    public static final int NORMAL_SHUTDOWN = 10;
    public static final int RESTART_EXITCODE = 11;
    public static final int NEWLY_CREATED_CONFIG = 12;

    //Non error, action required exit codes.
    public static final int UPDATE_LATEST_EXITCODE = 20;
    public static final int UPDATE_RECOMMENDED_EXITCODE = 21;

    //error exit codes.
    public static final int UNABLE_TO_CONNECT_TO_DISCORD = 30;
    public static final int BAD_USERNAME_PASS_COMBO = 31;
    public static final int NO_USERNAME_PASS_COMBO = 32;

    public static final String BUILD_DATE_RECOMMENDED_URL = "https://drone.io/github.com/DV8FromTheWorld/Yui/files/release/build-date-recommended.txt";
    public static final String BUILD_DATE_LATEST_URL = "https://drone.io/github.com/DV8FromTheWorld/Yui/files/release/build-date-latest.txt";
    public static final String BUILD_DATE_FILE_NAME = "build-date.txt";
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    private static Date BUILD_DATE;
    private static JDA api;
    private static List<IrcConnection> ircConnections;

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

    public static JDA getAPI()
    {
        return api;
    }

    public static IrcConnection getIrcConnection(String identifier)
    {
        for (IrcConnection irc : ircConnections)
        {
            if (irc.getIdentifier().equals(identifier))
                return irc;
        }
        return null;
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
            JDABuilder jdaBuilder = new JDABuilder(settings.getEmail(), settings.getPassword());
            Database.getInstance();
            Permissions.setupPermissions();
            ircConnections = new ArrayList<IrcConnection>();

            HelpCommand help = new HelpCommand();
            jdaBuilder.addListener(help.registerCommand(help));
            jdaBuilder.addListener(help.registerCommand(new TestCommand()));
            jdaBuilder.addListener(help.registerCommand(new SearchCommand()));
            jdaBuilder.addListener(help.registerCommand(new NyaaCommand()));
            jdaBuilder.addListener(help.registerCommand(new MyAnimeListCommand()));
            jdaBuilder.addListener(help.registerCommand(new AnimeNewsNetworkCommand()));
            jdaBuilder.addListener(help.registerCommand(new ReloadCommand()));
            jdaBuilder.addListener(help.registerCommand(new UpdateCommand()));
            jdaBuilder.addListener(help.registerCommand(new PermissionsCommand()));
            for (IrcConnectInfo info  : settings.getIrcConnectInfos())
            {
                IrcConnection irc = new IrcConnection(info);
                ircConnections.add(irc);
                jdaBuilder.addListener(irc);
            }

            if (settings.getProxyHost() != null && !settings.getProxyHost().isEmpty())
            {
                //Sets JDA's proxy settings
                jdaBuilder.setProxy(settings.getProxyHost(), Integer.valueOf(settings.getProxyPort()));

                //Sets the JVM level proxy settings.
                System.setProperty("http.proxyHost", settings.getProxyHost());
                System.setProperty("http.proxyPort", settings.getProxyPort());
                System.setProperty("https.proxyHost", settings.getProxyHost());
                System.setProperty("https.proxyPort", settings.getProxyPort());

            }

            //Login to Discord now that we are all setup.
            api = jdaBuilder.buildBlocking();
            Permissions.getPermissions().setBotAsOp(api.getSelfInfo());

            //Creates and Stores all Discord endpoints in our Manager.
            for (Guild guild : api.getGuilds())
            {
                for (TextChannel channel : guild.getTextChannels())
                {
                    EndPointManager.getInstance().createEndPoint(EndPointInfo.createFromDiscordChannel(channel));
                }
            }
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("No login details provided! Please give an email and password in the config file.");
            System.exit(NO_USERNAME_PASS_COMBO);
        }
        catch (LoginException e)
        {
            System.out.println("The Email and Password combination provided in the Config.json was incorrect.");
            System.out.println("Did you modify the Config.json after it was created?");
            System.exit(BAD_USERNAME_PASS_COMBO);
        }
        catch (InterruptedException e)
        {
            System.out.println("Our login thread was interrupted!");
            System.exit(UNABLE_TO_CONNECT_TO_DISCORD);
        }
//        catch (DiscordFailedToConnectException e)
//        {
//            System.out.println("We failed to connect to the Discord API. Do you have internet connection?");
//            System.out.println("Also double-check your Config.json for possible mistakes.");
//            System.exit(UNABLE_TO_CONNECT_TO_DISCORD);
//        }
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
