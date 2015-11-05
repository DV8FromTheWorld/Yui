package me.itsghost.jdiscord.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.talkable.GroupUser;

@Getter
@AllArgsConstructor
public class UserKickedEvent {
    private final Server server;
    private final GroupUser groupUser;
}
