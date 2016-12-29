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
package net.dv8tion.discord.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NyaaCommand extends Command
{
    public static final String NYAA_SEARCH_URL = "http://www.nyaa.se/?page=search&term=";

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
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

        sendMessage(e, NYAA_SEARCH_URL + StringUtils.join(args, "+", lastSortArg + 1, args.length)  + sortType + sortMethod);
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
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
                ".nyaa *</sortType> </sortMethod> <search terms>*\n"
                + "__Sort Types:__\n"
                + "  /name     - sorts by the name.\n"
                + "  /date     - sort search by date. **default**\n"
                + "  /seeders  - sort by the amount of seeders.\n"
                + "     *{seed, seeds, seeder, seeders}*\n"
                + "  /leechers - sort by the amount of leechers.\n"
                + "     *{leech, leecher, leechers}*\n"
                + "  /download - sorts by the amount of times downloads.\n"
                + "     *{dl, dls, download, downloads}*\n"
                + "  /size     - sorts by the total size of the torrent.\n"
                + "\n"
                + "__Sort Methods:__\n"
                + "  /asc      - sorts the type in ascending fashion (smallest to largest, a-Z, 0-9)\n"
                + "     *{asc, ascend, up}\n"
                + "  /desc     - sorts the type in descending fashion (largest to smallest, Z-a, 9-0) **default**\n"
                + "     *{des, desc, descend, down}\n"
                + "\n"
                + "__Example:__   .nyaa steins gate\n"
                + " - Returns search of \"steins gate\", sorts by date, descending. (newest torrents first)\n"
                + "__Example 2:__ .nyaa /seed /down familar of zero\n"
                + " - returns search of \"familar of zero\", sorts by seeders, descending. (torrents with most seeders first)\n"
                + "__Example 3:__ .nyaa /name /ascend one punch man commie\n"
                + " - returns search of \"one punch man\", sorts by name, ascending. (torrents sorted alphabetically)");
    }
}
