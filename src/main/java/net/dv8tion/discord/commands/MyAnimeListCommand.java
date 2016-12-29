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

import net.dv8tion.discord.util.Downloader;
import net.dv8tion.discord.util.GoogleSearch;
import net.dv8tion.discord.util.SearchResult;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MyAnimeListCommand extends Command
{
    public static final String ANIME_URL = "http://myanimelist.net/anime/";
    public static final String MANGA_URL = "http://myanimelist.net/manga/";
    public static final String CHARACTER_URL = "http://myanimelist.net/character/";

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        List<SearchResult> results = GoogleSearch.performSearch(
                "018291224751151548851:pwowlyhmpyc",
                StringUtils.join(args, "+", 1, args.length));

        sendMessage(e, results.get(0).getSuggestedReturn());
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".mal");
    }

    @Override
    public String getDescription()
    {
        return "Searches the <http://myanimelist.net> anime/manga database for anime and manga.";
    }

    @Override
    public String getName()
    {
        return "MyAnimeList Search";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
                ".mal *<search terms>*\n"
                + "__Example:__ .mal sao\n"
                + " - This will return the entry for Sword Art Online.\n"
                + "__Example 2:__ .mal magi kingdom of magic\n"
                + " - This will return the entry for Magi - The Kingdom of Magic.");
    }

    @SuppressWarnings("unused")
    private String handleSearch(SearchResult result)
    {
        String url = result.getUrl();
        if (url.contains(ANIME_URL))
        {
            System.out.println("this is anime");
            String webpage = Downloader.webpage("http://myanimelist.net/manga/75989/");
            System.out.println(webpage);
        }
        else if (url.contains(MANGA_URL))
        {
            System.out.println("this is manga");
            String webpage = Downloader.webpage(url);
            System.out.println(webpage);
        }
        else if (url.contains(CHARACTER_URL))
        {
            System.out.println("this is character");
        }
        else
        {
        }
        return null;
    }
}
