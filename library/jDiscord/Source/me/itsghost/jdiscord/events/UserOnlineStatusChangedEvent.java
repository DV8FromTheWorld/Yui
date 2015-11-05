package me.itsghost.jdiscord.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.itsghost.jdiscord.OnlineStatus;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;

@Getter
@AllArgsConstructor
public class UserOnlineStatusChangedEvent {
    private final User user;
    private final OnlineStatus status;
    private final String game;
}
