package net.dv8tion.discord.bridge;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class EndPointSerializer implements JsonSerializer<EndPoint>, JsonDeserializer<EndPoint>
{

    @Override
    public JsonElement serialize(EndPoint src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public EndPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        String jsonString = json.getAsString();
        if (!jsonString.contains(EndPoint.SEPARATOR))
        {
            System.err.println("PROBLEM LOADING BRIDGE: '" + jsonString + "'");
            System.err.printf("Correct Format:  \"connectorId%schannelId\" : \"connectorId%schannelId\"\n", EndPoint.SEPARATOR, EndPoint.SEPARATOR);
            System.err.println("This specific bridge will be disabled. Please fix the config to enable!");
            return null;
        }
        String connectorId = jsonString.substring(0, jsonString.indexOf(EndPoint.SEPARATOR));
        String channelId = jsonString.substring(jsonString.indexOf(EndPoint.SEPARATOR) + EndPoint.SEPARATOR.length());

        return new EndPoint(connectorId, channelId);
    }
}
