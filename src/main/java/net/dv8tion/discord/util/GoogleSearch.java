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
package net.dv8tion.discord.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Random;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GoogleSearch
{
    private JsonArray results;

    public GoogleSearch(String terms)
    {
        performSearch(terms.replaceAll(" ", "+"));
    }

    public String getTitle(int resultIndex)
    {
        String title = results.get(resultIndex).getAsJsonObject().get("title").toString();
        return cleanString(title);
    }

    public String getContent(int resultIndex)
    {
        String content =  results.get(resultIndex).getAsJsonObject().get("content").toString();
        return cleanString(content);
    }

    public String getUrl(int resultIndex)
    {
        String url = results.get(resultIndex).getAsJsonObject().get("url").toString();
        url = cleanString(url);
        try
        {
            return URLDecoder.decode(url, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return url;
    }

    public String getSuggestedReturn()
    {
        return getUrl(0) + " - *" + getTitle(0) + "*: \"" + getContent(0) + "\"";
    }

    public int getResultCount()
    {
        return results.size();
    }

    private void performSearch(String terms) {
        try {
            StringBuilder searchURLString = new StringBuilder();
            searchURLString.append("https://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=");
            searchURLString.append(terms);

            URL searchURL = new URL(searchURLString.toString());
            URLConnection conn = searchURL.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:39.0) Gecko/20100101 " + randomName(10));
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                json.append(line).append("\n");
            }
            in.close();
            JsonElement element = new JsonParser().parse(json.toString());
            results = element.getAsJsonObject().getAsJsonObject("responseData").getAsJsonArray("results");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String cleanString(String uncleanString)
    {
        return StringEscapeUtils.unescapeJava(
                StringEscapeUtils.unescapeHtml4(
                        uncleanString
                            .replaceAll("\\s+", " ")
                            .replaceAll("\\<.*?>", "")
                            .replaceAll("\"", "")));
    }

    private String randomName(int randomLength)
    {
        char[] characters = new char[]
                {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
                '1','2','3','4','5','6','7','8','9','0'};

        Random rand = new Random();
        StringBuilder builder = new StringBuilder();
        builder.append("DiscordBot/");
        for (int i = 0; i < randomLength; i++)
        {
            builder.append(characters[rand.nextInt(characters.length)]);
        }
        return builder.toString();
    }
}
