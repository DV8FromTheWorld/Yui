package me.itsghost.jdiscord.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.itsghost.jdiscord.talkable.Group;

@Getter
@AllArgsConstructor
public class ChannelUpdatedEvent {
    private final Group group;
}
