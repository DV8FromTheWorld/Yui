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
package net.dv8tion.discord.bridge;

import com.google.common.base.Strings;
import net.dv8tion.discord.bridge.endpoint.EndPoint;
import net.dv8tion.discord.bridge.endpoint.EndPointInfo;
import net.dv8tion.discord.bridge.endpoint.EndPointManager;
import net.dv8tion.discord.bridge.endpoint.EndPointMessage;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.*;
import net.dv8tion.jda.core.hooks.EventListener;
import org.apache.http.util.Args;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.SocketConnectEvent;

import java.io.IOException;

public class IrcConnection extends ListenerAdapter<PircBotX> implements EventListener
{
    public static final int MESSAGE_DELAY_AMOUNT = 250;

    private final IrcConnectInfo info;
    private String identifier;
    private Thread botThread;
    private PircBotX bot;

    public IrcConnection(IrcConnectInfo info)
    {
        this.info = info;
        identifier = info.getIdentifier();
        Builder<PircBotX> builder = info.getIrcConfigBuilder();
        builder.addListener(this);
        builder.setMessageDelay(MESSAGE_DELAY_AMOUNT);
        builder.setAutoReconnect(true);
        builder.setAutoNickChange(true);
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
        bot.stopBotReconnect();
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
    public void onConnect(ConnectEvent<PircBotX> event)
    {
        //If, after connection, we don't have the defined nick AND we have auth info, attempt to ghost
        // account using our desired nick and switch to our desired nick.
        if (!event.getBot().getUserBot().getNick().equals(info.getNick())
                && !Strings.isNullOrEmpty(info.getIdentPass()))
        {
            event.getBot().sendRaw().rawLine("NICKSERV GHOST " + info.getNick() + " " + info.getIdentPass());
            event.getBot().sendIRC().changeNick(info.getNick());
        }
    }

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
    public void onJoin(JoinEvent<PircBotX> event)
    {
        if (event.getBot().getUserBot().equals(event.getUser()))
            EndPointManager.getInstance().createEndPoint(EndPointInfo.createFromIrcChannel(identifier, event.getChannel()));
    }

    // -- Discord --

    @Override
    public void onEvent(Event event)
    {
        Message msg;
        if (event instanceof GuildMessageReceivedEvent)
        {
            msg = ((GuildMessageReceivedEvent) event).getMessage();
        }
        else if (event instanceof GuildMessageUpdateEvent)
        {
            msg = ((GuildMessageUpdateEvent) event).getMessage();
        }
        else
        {
            return;
        }

        //Basically: If we are the ones that sent the message, don't send it to IRC.
        if (event.getJDA().getSelfUser().equals(msg.getAuthor()))
            return;

        //If this returns null, then this EndPoint isn't part of a bridge.
        EndPoint endPoint = BridgeManager.getInstance().getOtherEndPoint(EndPointInfo.createFromDiscordChannel(msg.getTextChannel()));
        if (endPoint != null)
        {
            EndPointMessage message = EndPointMessage.createFromDiscordEvent(msg);
            endPoint.sendMessage(message);
        }
    }
}
