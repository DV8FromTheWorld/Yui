package me.itsghost.jdiscord.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.message.Message;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;

@Getter
@AllArgsConstructor
public class UserChatEvent  {
    private final Group group;
    private final GroupUser user;
    private Message msg;

    public Server getServer(){
        return group.getServer();
    }
}
