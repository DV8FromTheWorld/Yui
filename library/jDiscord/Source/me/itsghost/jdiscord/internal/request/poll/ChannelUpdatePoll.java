package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.ChannelDeletedEvent;
import me.itsghost.jdiscord.events.ChannelUpdatedEvent;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.impl.GroupImpl;
import me.itsghost.jdiscord.talkable.Group;
import org.json.JSONObject;

public class ChannelUpdatePoll implements Poll {
    private DiscordAPIImpl api;

    public ChannelUpdatePoll(DiscordAPIImpl api) {
        this.api = api;
    }

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server){
        GroupImpl group = (GroupImpl)server.getGroupById(content.getString("id"));
        group.setName(content.getString("name"));
        api.getEventManager().executeEvent(new ChannelUpdatedEvent(group));
    }
}
