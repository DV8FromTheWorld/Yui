package net.dv8tion.discord.bridge;

import java.io.IOException;

import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.events.UserChatEvent;
import net.dv8tion.discord.bridge.endpoint.EndPoint;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointManager;

import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;

public class IrcConnection extends ListenerAdapter<PircBotX> implements EventListener
{
    private String identifier;
    private Thread botThread;
    private PircBotX bot;

    public IrcConnection(IRCConnectInfo info)
    {
        identifier = info.getIdentifier();
        Builder<PircBotX> builder = info.getIrcConfigBuilder();
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

    public String getIdentifier()
    {
        return identifier;
    }

    public PircBotX getIrcBot()
    {
        return bot;
    }
    // -----  Events -----

    // -- IRC --
    @Override
    public void onMessage(MessageEvent<PircBotX> event)
    {
        EndPoint p = BridgeManager.getInstance().getOtherEndPoint(EndPointInfo.createFromIrcChannel(identifier, event.getChannel()));
        System.out.println("I found this bridge: " + p.toEndPointInfo().getChannelId());
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
            EndPointManager.getInstance().createEndPoint(EndPointInfo.createFromIrcChannel(identifier, event.getChannel()));
    }

    // -- Discord --

    public void onDiscordGroupChat(UserChatEvent e)
    {
        EndPoint p = BridgeManager.getInstance().getOtherEndPoint(EndPointInfo.createFromDiscordGroup(e.getGroup()));
        if (p != null)
            System.out.println("I found this bridge: " + p.toEndPointInfo().getChannelId());
    }
}
