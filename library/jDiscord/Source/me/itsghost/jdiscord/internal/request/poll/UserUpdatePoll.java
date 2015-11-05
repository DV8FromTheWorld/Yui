package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.events.ChannelDeletedEvent;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.impl.UserImpl;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class UserUpdatePoll implements Poll {
    private DiscordAPIImpl api;

    public UserUpdatePoll(DiscordAPIImpl api) {
        this.api = api;
    }

    @Override
    public void process(JSONObject content, JSONObject rawRequest, Server server){
        JSONObject user = content.getJSONObject("user");
        JSONArray rolesArray = content.getJSONArray("roles");
        GroupUser gUser = server.getGroupUserById(user.getString("id"));

        ((UserImpl)gUser.getUser()).setUsername(user.getString("username"));
        ((UserImpl)gUser.getUser()).setAvatarId(user.getString("avatar"));
        ((UserImpl)gUser.getUser()).setAvatar("https://cdn.discordapp.com/avatars/" + api.getSelfInfo().getId() + "/" + (user.isNull("avatar") ? "" : user.getString("avatar")) + ".jpg");


        for (int i = 0; i < rolesArray.length(); i++) {
            JSONObject roleObj = rolesArray.getJSONObject(i);
            gUser.setRole(roleObj.getString("name"));
        }
    }
}
