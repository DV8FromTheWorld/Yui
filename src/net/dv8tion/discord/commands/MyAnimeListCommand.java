package net.dv8tion.discord.commands;

import java.util.Arrays;
import java.util.List;

import net.dv8tion.discord.GoogleSearch;

import org.apache.commons.lang3.StringUtils;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;

public class MyAnimeListCommand extends Command
{

    @Override
    public void onChat(UserChatEvent e)
    {
        String[] args = e.getMsg().toString().split(" ");
        if (aliases().contains(args[0]))
        {
            GoogleSearch search = new GoogleSearch(
                    String.format("%s+%s",
                            StringUtils.join(args, "+", 1, args.length),
                            "site:myanimelist.net"));

            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": " + search.getSuggestedReturn())
                .build());
        }
    }

    @Override
    public List<String> aliases()
    {
        return Arrays.asList(".mal");
    }

    @Override
    public String commandDescription()
    {
        return "Searches the http://myanimelist.net anime/manga database for anime and manga.";
    }

    @Override
    public String helpMessage()
    {
        return null;
    }

}
