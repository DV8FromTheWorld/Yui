package me.itsghost.jdiscord.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.talkable.Group;

@Getter
@AllArgsConstructor
public class ChannelDeletedEvent {
    private final Group group;
}
