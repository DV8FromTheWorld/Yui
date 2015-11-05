package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.Server;
import org.json.JSONObject;

public interface Poll {
    void process(JSONObject content, JSONObject rawRequest, Server server);
}
