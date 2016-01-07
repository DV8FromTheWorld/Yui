package net.dv8tion.discord.bridge;

import java.io.IOException;

import net.dv8tion.discord.bridge.endpoint.EndPoint;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointManager;
import net.dv8tion.discord.bridge.endpoint.EndPointMessage;

import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.message.guild.*;
import net.dv8tion.jda.hooks.EventListener;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;

public class IrcConnection extends ListenerAdapter<PircBotX> implements EventListener
{
    public static final int MESSAGE_DELAY_AMOUNT = 250;

    private String identifier;
    private Thread botThread;
    private PircBotX bot;

    public IrcConnection(IrcConnectInfo info)
    {
        identifier = info.getIdentifier();
        Builder<PircBotX> builder = info.getIrcConfigBuilder();
        builder.addListener(this);
        builder.setMessageDelay(MESSAGE_DELAY_AMOUNT);
        builder.setAutoReconnect(true);
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
        //Specific to the the Imaginescape IRC/Discord channel. Dumb minecraft server spits out an empty message that is really annoying.
        if (event.getUser().getNick().equals("IServer") && event.getMessage().equals("[Server]"))
            return;

        //If this returns null, then this EndPoint isn't part of a bridge.
        EndPoint endPoint = BridgeManager.getInstance().getOtherEndPoint(EndPointInfo.createFromIrcChannel(identifier, event.getChannel()));
        if (endPoint != null)
        {
            EndPointMessage message = EndPointMessage.createFromIrcEvent(event);
            endPoint.sendMessage(message);
        }
    }

    @Override
    public void onConnect(ConnectEvent<PircBotX> event)
    {

    }

    @Override
    public void onJoin(JoinEvent<PircBotX> event)
    {
        if (event.getBot().getUserBot().equals(event.getUser()))
            EndPointManager.getInstance().createEndPoint(EndPointInfo.createFromIrcChannel(identifier, event.getChannel()));
    }

    // -- Discord --

    @Override
    public void onEvent(Event event)
    {
        //We only deal with TextChannel Message events
        if (!(event instanceof GenericGuildMessageEvent))
            return;

        //Don't care about deleted messages or embeds.
        if (event instanceof GuildMessageDeleteEvent || event instanceof GuildMessageEmbedEvent)
            return;

        GenericGuildMessageEvent e = (GenericGuildMessageEvent) event;

        //Basically: If we are the ones that sent the message, don't send it to IRC.
        if (event.getJDA().getSelfInfo().getId().equals(e.getAuthor().getId()))
            return;

        //If this returns null, then this EndPoint isn't part of a bridge.
        EndPoint endPoint = BridgeManager.getInstance().getOtherEndPoint(EndPointInfo.createFromDiscordChannel(e.getChannel()));
        if (endPoint != null)
        {
            EndPointMessage message = EndPointMessage.createFromDiscordEvent(e);
            endPoint.sendMessage(message);
        }
    }
}
