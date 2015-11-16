package net.dv8tion.discord.commands;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.events.APILoadedEvent;
import me.itsghost.jdiscord.events.UserChatEvent;

public class ReloadCommand extends Command
{
    private DiscordAPI api;
    private Thread thread;

    public ReloadCommand(DiscordAPI api)
    {
        this.api = api;
        this.thread = null;
    }

    @Override
    public void onChat(UserChatEvent e)
    {
    }

    public void onApiLoaded(APILoadedEvent e)
    {
        try
        {
            Field requestManager = api.getClass().getDeclaredField("requestManager");
            requestManager.setAccessible(true);
            Object requestManagerObj = requestManager.get(api);

            Field socketClient = requestManagerObj.getClass().getDeclaredField("socketClient");
            socketClient.setAccessible(true);
            Object socketClientObj = socketClient.get(requestManagerObj);

            Field readyPoll = socketClientObj.getClass().getDeclaredField("readyPoll");
            readyPoll.setAccessible(true);
            Object readyPollObj = readyPoll.get(socketClientObj);

            Field thread = readyPollObj.getClass().getDeclaredField("thread");
            thread.setAccessible(true);
            Object threadObj = thread.get(readyPollObj);
            if (threadObj instanceof Thread)
            {
                this.thread = (Thread) threadObj;
            }
        }
        catch (IllegalArgumentException | IllegalAccessException
                | NoSuchFieldException | SecurityException e1)
        {
            System.out.println("If you see this message, please report it to the developer. ReflectionFailure (ReloadCommand");
            e1.printStackTrace();
        }
        if (this.thread == null)
        {
            System.out.println("Unable to capture bot thread, ReloadCommand has been disabled.");
        }
    }

    @Override
    public List<String> aliases()
    {
        return Arrays.asList(new String[] {".reload"});
    }

    @Override
    public String commandDescription()
    {
        return "Kills the current instance and launches a fresh instance of this bot.";
    }

    @Override
    public String helpMessage()
    {
        return null;
    }
}
