package me.itsghost.jdiscord.talkable;

import me.itsghost.jdiscord.OnlineStatus;

public interface User {
    Group getGroup();

    String getAvatar();

    String getUsername();

    String getId();

    String getGame();

    OnlineStatus getOnlineStatus();
}
