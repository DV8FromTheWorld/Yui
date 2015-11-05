package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.events.UserDeletedMessageEvent;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.impl.MessageImpl;
import me.itsghost.jdiscord.message.Message;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

public class DeleteMessagePoll implements Poll {
    private DiscordAPIImpl api;

    public DeleteMessagePoll(DiscordAPIImpl api) {
        this.api = api;
    }

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server) {
        try {
            Group group = api.getGroupById(content.getString("channel_id"));
            String id = content.getString("id");
            api.getEventManager().executeEvent(new UserDeletedMessageEvent(group, id));
        }catch(Exception e){
            api.log("Failed to process message:\n >" + content);
        }
    }
}
