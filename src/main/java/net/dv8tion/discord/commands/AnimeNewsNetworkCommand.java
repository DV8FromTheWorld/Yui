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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimeNewsNetworkCommand extends Command
{
    public static final String ANIME_URL = "http://www.animenewsnetwork.com/encyclopedia/anime.php?id=";
    public static final String MANGA_URL = "http://www.animenewsnetwork.com/encyclopedia/manga.php?id=";
    public static final String ANN_API_URL = "http://cdn.animenewsnetwork.com/encyclopedia/api.xml?title=";

    private static final String MAIN_TITLE_REGEX = "(?<=type=\"Main title\" lang=\"..\">).*?(?=</info>)";
    private static final String ALT_TITLE_REGEX = "(?<=type=\"Alternative title\" lang=\"JA\">).*?(?=</info>)";
    private static final String SUMMARY_REGEX = "(?<=type=\"Plot Summary\">).*?(?=</info>)";

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        List<SearchResult> results = GoogleSearch.performSearch(
                "018291224751151548851:g6kjw0k_cp8",
                StringUtils.join(args, "+", 1, args.length));

        sendMessage(e, handleSearch(results.get(0)));
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".ann");
    }

    @Override
    public String getDescription()
    {
        return "Searches the <http://animenewsnetwork.com> database of Anime and Manga.";
    }

    @Override
    public String getName()
    {
        return "AnimeNewsNetwork Search";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
                ".ann *<search terms>*\n"
                + "__Example:__ .ann Steins;Gate\n"
                + " - This will return the AnimeNewsNetwork page for the anime Steins;Gate\n"
                + "__Example 2:__ .ann Boku no Hero Academia Manga\n"
                + " - This will return the manga page for Boku no Hero Academia (hopefully)");
    }

    private String handleSearch(SearchResult result)
    {
        String url = result.getUrl();
        if (url.contains(ANIME_URL) || url.contains(MANGA_URL))
        {
            String title = null;
            String altTitle = null;
            String summary = null;
            String imageUrl = null;

            String id = url.replace(ANIME_URL, "").replace(MANGA_URL, "");
            String xmlReturn = StringEscapeUtils.unescapeHtml4(Downloader.webpage(ANN_API_URL + id));

            Pattern p = Pattern.compile(MAIN_TITLE_REGEX);
            Matcher m = p.matcher(xmlReturn);
            if (m.find())
                title = m.group();
            else
                title = result.getTitle();

            Pattern p2 = Pattern.compile(ALT_TITLE_REGEX);
            Matcher m2 = p2.matcher(xmlReturn);
            if (m2.find())
                altTitle = m2.group();

            Pattern p3 = Pattern.compile(SUMMARY_REGEX);
            Matcher m3 = p3.matcher(xmlReturn);
            if (m3.find())
                summary = m3.group();
            else
                summary = result.getContent();

            Pattern p4 = Pattern.compile("type=\"Picture\".*?>.*?</info>");
            Matcher m4 = p4.matcher(xmlReturn);
            if (m4.find())
            {
                Pattern p41 = Pattern.compile("(?<=img src=\").*?(?=\" width=\"...?.?.?\" height=\"...?.?.?\"/>)");
                Matcher m41 = p41.matcher(m4.group(0));

                while (m41.find())
                {
                    imageUrl = m41.group(0);
                    if (m41.group(0).contains("/thumbnails/max"))
                    {
                        break;
                    }
                }
            }

            return String.format("%s\n**Title:** %s\n%s\n%s",
                    url,
                    title + (altTitle == null ? "" : ("   **JPN Title:** " + altTitle)),
                    summary,
                    imageUrl != null ? imageUrl : "No Image Found");
        }
        else
        {
            return result.getSuggestedReturn();
        }
    }
}
