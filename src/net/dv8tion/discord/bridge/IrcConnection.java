package net.dv8tion.discord.bridge;

import java.io.IOException;
import java.util.ArrayList;

import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.events.UserChatEvent;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointManager;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;

public class IrcConnection extends ListenerAdapter<PircBotX> implements EventListener
{
    private Thread botThread;
    private PircBotX bot;
    private ArrayList<Bridge> bridges;

    public IrcConnection(Configuration.Builder<PircBotX> builder)
    {
        builder.addListener(this);
        builder.setMessageDelay(250);  //TODO: Make this configurable.
        bot = new PircBotX(builder.buildConfiguration());
        this.open();
    }

    public void open()
    {
        if (botThread != null)
            throw new IllegalStateException("We tried to create another bot thread before killing the current one!");

        botThread = new Thread()
        {
            public void run()
            {
                try
                {
                    bot.startBot();
                }
                catch (IOException | IrcException e)
                {
                    System.err.println("Yeah.. idk. Sorry");
                    e.printStackTrace();
                }
            }
        };
        botThread.start();
    }

    public void close(String reason)
    {
        //TODO: Cleanup the EndPoints of this connection in EndPointManager.
        bot.sendIRC().quitServer(reason);
    }

    @Override
    public void onMessage(MessageEvent<PircBotX> event)
    {

    }

    @Override
    public void onConnect(ConnectEvent<PircBotX> event)
    {
        System.out.println("We connected");
    }

    @Override
    public void onJoin(JoinEvent<PircBotX> event)
    {
        if (event.getBot().getUserBot().equals(event.getUser()))
            EndPointManager.getInstance().getOrCreate(EndPointInfo.createFromIrcChannel(event.getChannel()));
    }

    public void onDiscordGroupChat(UserChatEvent e)
    {

    }
}
