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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.discord.bridge.IrcConnectInfo;
import net.dv8tion.discord.bridge.IrcConnection;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointManager;
import net.dv8tion.discord.commands.*;
import net.dv8tion.discord.util.Database;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.LoginException;

public class Yui
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
        String path = Yui.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(path, "UTF-8");

        //This is code especially written for running and testing this program in an IDE that doesn't compile to .jar when running.
        if (!decodedPath.endsWith(".jar"))
        {
            return new File("Yui.jar");
        }
        return new File(decodedPath);   //We use File so that when we send the path to the ProcessBuilder, we will be using the proper System path formatting.
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

            JDABuilder jdaBuilder = new JDABuilder().setBotToken(settings.getBotToken());
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
            jdaBuilder.addListener(help.registerCommand(new EvalCommand()));
            jdaBuilder.addListener(help.registerCommand(new RollCommand()));
            jdaBuilder.addListener(help.registerCommand(new InfoCommand()));
            jdaBuilder.addListener(help.registerCommand(new UptimeCommand()));

            for (IrcConnectInfo info  : settings.getIrcConnectInfos())
            {
                if (info.getHost() == null || info.getHost().isEmpty())
                {
                    System.out.println("Skipping IRC connection '" + info.getIdentifier() + "' because no Host was provided.");
                    continue;
                }
                if (info.getNick() == null || info.getNick().isEmpty())
                {
                    System.out.println("Skipping IRC connection '" + info.getIdentifier() + "' because no Nick was provided.");
                    continue;
                }
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

            api.addEventListener(help.registerCommand(new TodoCommand(api)));

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
            System.out.println("No login details provided! Please provide a botToken in the config.");
            System.exit(NO_USERNAME_PASS_COMBO);
        }
        catch (LoginException e)
        {
            System.out.println("The botToken provided in the Config.json was incorrect.");
            System.out.println("Did you modify the Config.json after it was created?");
            System.exit(BAD_USERNAME_PASS_COMBO);
        }
        catch (InterruptedException e)
        {
            System.out.println("Our login thread was interrupted!");
            System.exit(UNABLE_TO_CONNECT_TO_DISCORD);
        }
    }

    private static void relaunchInUTF8() throws InterruptedException, UnsupportedEncodingException
    {
        System.out.println("BotLauncher: We are not running in UTF-8 mode! This is a problem!");
        System.out.println("BotLauncher: Relaunching in UTF-8 mode using -Dfile.encoding=UTF-8");

        String[] command = new String[] {"java", "-Dfile.encoding=UTF-8", "-jar", Yui.getThisJarFile().getAbsolutePath()};

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
