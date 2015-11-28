package net.dv8tion.discord.commands;

import java.util.Arrays;
import java.util.List;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;

import org.apache.commons.lang3.StringUtils;

public class NyaaCommand extends Command
{
    public static final String NYAA_SEARCH_URL = "http://www.nyaa.se/?page=search&term=";

    @Override
    public void onChat(UserChatEvent e)
    {
        if (!containsCommand(e.getMsg()))
            return;

        e.getGroup().sendMessage(new MessageBuilder()
        .addString(createUrl(commandArgs(e.getMsg())))
        .build());
//        Downloader.webpage("");
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".nyaa");
    }

    @Override
    public String getDescription()
    {
        return "Searches the <http://nyaa.se> torrent tracker for torrents.";
    }

    @Override
    public String getName()
    {
        return "Nyaa Torrent Search";
    }

    @Override
    public String getUsageInstructions()
    {
        // TODO Auto-generated method stub
        return null;
    }

    private String createUrl(String[] args)
    {
        String sortType = null;
        String sortMethod = null;
        int lastSortArg = 0;
        for (int i = 1; i < args.length && (sortType == null || sortMethod == null); i++)
        {
            if (!args[i].contains("/"))
            {
                break; //If this arg doesn't have a / then none of the following args -should- either.
            }

            //If we haven't already found a type to sort by, see if one was provided. Otherwise, skip.
            if (sortType == null)
            {
                String arg = args[i].substring(1).toLowerCase();    //Cuts off the preceeding /
                switch(arg)
                {
                    case "date":
                        sortType = "&sort=1";
                        lastSortArg = i;
                        break;
                    case "seed":
                    case "seeds":
                    case "seeder":
                    case "seeders":
                        sortType = "&sort=2";
                        lastSortArg = i;
                        break;
                    case "leech":
                    case "leecher":
                    case "leechers":
                    case "leach":           //Because people can't spell
                    case "leacher":         //
                    case "leachers":        //
                        sortType = "&sort=3";
                        lastSortArg = i;
                        break;
                    case "dl":
                    case "dls":
                    case "download":
                    case "downloads":
                        sortType = "&sort=4";
                        lastSortArg = i;
                        break;
                    case "size":
                        sortType = "&sort=5";
                        lastSortArg = i;
                        break;
                    case "name":
                        sortType = "&sort=6";
                        lastSortArg = i;
                        break;
                }
            }

            //If we haven't already found a method of sorting (asc or desc), see if one was provided. Otherwise, skip.
            if (sortMethod == null)
            {
                String arg = args[i].substring(1).toLowerCase();    //Cuts off the preceeding /
                switch(arg)
                {
                    case "asc":
                    case "ascend":
                    case "up":
                    case "asend":           //Because people can't spell
                    case "acend":           //
                        sortMethod = "&order=2";
                        lastSortArg = i;
                        break;
                    case "des":
                    case "desc":
                    case "descend":
                    case "down":
                    case "decs":            //Because people can't spell
                    case "decend":          //
                    case "desend":          //
                    case "decsend":         //
                        sortMethod = "&order=1";
                        lastSortArg = i;
                        break;
                }
            }
        }
        sortType = sortType == null ? "&sort=2" : sortType;
        sortMethod = sortMethod == null ? "&order=1" : sortMethod;
        return NYAA_SEARCH_URL + StringUtils.join(args, "+", lastSortArg + 1, args.length)  + sortType + sortMethod;
    }
}
