package net.dv8tion.discord;

import net.dv8tion.discord.commands.TestCommand;
import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.DiscordBuilder;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;

public class Bot
{

    public static void main(String[] args)
    {
        try
        {
            DiscordAPI api = new DiscordBuilder("email", "password").build().login();
            api.getEventManager().registerListener(new TestCommand());
        }
        catch (NoLoginDetailsException e)
        {
            e.printStackTrace();
        }
        catch (BadUsernamePasswordException e)
        {
            e.printStackTrace();
        }
        catch (DiscordFailedToConnectException e)
        {
            e.printStackTrace();
        }
    }
}
