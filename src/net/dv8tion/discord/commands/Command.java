package net.dv8tion.discord.commands;

import java.util.List;

import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.events.UserChatEvent;

public abstract class Command implements EventListener 
{
    public abstract void onChat(UserChatEvent e);
    public abstract List<String> aliases();
    public abstract String commandDescription();
    public abstract String helpMessage();
}
