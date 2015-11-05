package me.itsghost.jdiscord.talkable;

import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.message.Message;
import me.itsghost.jdiscord.message.MessageHistory;

public interface Group {
    String getId();

    String getName();

    Server getServer();

    MessageHistory getMessageHistory();

    Message sendMessage(String message);

    Message sendMessage(Message message);
}
