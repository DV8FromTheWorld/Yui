package net.dv8tion.discord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

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
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:39.0) Gecko/20100101 RavenBot/2.0");
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
}
