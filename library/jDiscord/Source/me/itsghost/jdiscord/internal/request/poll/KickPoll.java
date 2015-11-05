package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.UserKickedEvent;
import me.itsghost.jdiscord.talkable.GroupUser;
import org.json.JSONObject;


public class KickPoll implements Poll {
    private DiscordAPIImpl api;

    public KickPoll(DiscordAPIImpl api) {
        this.api = api;
    }

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server) {
        JSONObject user = content.getJSONObject("user");
        GroupUser gUser = server.getGroupUserById(user.getString("id"));
        server.getConnectedClients().remove(gUser);
        api.getEventManager().executeEvent(new UserKickedEvent(server, gUser));
    }
}
