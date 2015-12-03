package net.dv8tion.discord.handlers;

import java.io.IOException;

import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.events.APILoadedEvent;
import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.talkable.Group;
import net.dv8tion.discord.Bot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class IRCHandler implements EventListener
{
    public Group group;
    public PircBotX bot;

    public void onChat(UserChatEvent e)
    {
        //ServerId: 107563502712954880   - GroupId: 111277570581856256
        if (e.getGroup().getId().equals(group.getId()) && !e.getUser().getUser().getId().equals(Bot.getAPI().getSelfInfo().getId()))
        {
            bot.sendIRC().message("#nubtards", String.format("<%s> %s",
                    e.getUser().getUser().getUsername(),
                    e.getMsg().toString()));
        }
    }

    @SuppressWarnings("unchecked")
    public void onAPILoaded(APILoadedEvent e)
    {
        for (Group g : Bot.getAPI().getServerById("107563502712954880").getGroups())
        {
            if (g.getId().equals("111277570581856256"))
//            if (g.getName().equals("main"))
            {
                IRCHandler.this.group = g;
                break;
            }
        }
        //Configure what we want our bot to do
        @SuppressWarnings({ "rawtypes"})
        Configuration configuration = new Configuration.Builder()
        .setName("Yui") //Set the nick of the bot. CHANGE IN YOUR CODE
        .setAutoNickChange(true)
        .setRealName("Yui-MHCP001")
        .setMessageDelay(100)
        .setServer("irc.esper.net", 6667)
        .addAutoJoinChannel("#nubtards") //Join the official #pircbotx channel
        .addListener(new ListenerAdapter()
        {
            @Override
            public void onMessage(MessageEvent event) {
//                System.out.print("I did a thing");
                System.out.println("<" + event.getUser().getNick() + "> " + event.getMessage());
                group.sendMessage("<" + event.getUser().getNick() + "> " + event.getMessage());
            }
        }) //Add our listener that will be called on Events
        .buildConfiguration();

        IRCHandler.this.bot = new PircBotX(configuration);
        try
        {
            bot.startBot();
        }
        catch (IOException | IrcException e1)
        {
            e1.printStackTrace();
        }
    }
}
