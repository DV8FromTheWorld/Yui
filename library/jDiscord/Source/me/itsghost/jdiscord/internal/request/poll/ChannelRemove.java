package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.ChannelDeletedEvent;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.talkable.Group;
import org.json.JSONObject;

public class ChannelRemove implements Poll {
    private DiscordAPIImpl api;

    public ChannelRemove(DiscordAPIImpl api) {
        this.api = api;
    }

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server){
        Group group = server.getGroupById(content.getString("id"));
        server.getGroups().remove(group);
        api.getEventManager().executeEvent(new ChannelDeletedEvent(group));
    }
}
