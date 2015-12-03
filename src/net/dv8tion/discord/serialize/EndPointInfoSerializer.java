package net.dv8tion.discord.serialize;

import java.lang.reflect.Type;

import net.dv8tion.discord.bridge.endpoint.EndPointInfo;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class EndPointInfoSerializer implements JsonSerializer<EndPointInfo>, JsonDeserializer<EndPointInfo>
{

    @Override
    public JsonElement serialize(EndPointInfo src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public EndPointInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        String jsonString = json.getAsString();
        if (!jsonString.contains(EndPointInfo.SEPARATOR))
        {
            System.err.println("PROBLEM LOADING BRIDGE: '" + jsonString + "'");
            System.err.printf("Correct Format:  \"connectorId%schannelId\" : \"connectorId%schannelId\"\n", EndPointInfo.SEPARATOR, EndPointInfo.SEPARATOR);
            System.err.println("This specific bridge will be disabled. Please fix the config to enable!");
            return null;
        }
        String connectorId = jsonString.substring(0, jsonString.indexOf(EndPointInfo.SEPARATOR));
        String channelId = jsonString.substring(jsonString.indexOf(EndPointInfo.SEPARATOR) + EndPointInfo.SEPARATOR.length());

        return new EndPointInfo(connectorId, channelId);
    }
}
