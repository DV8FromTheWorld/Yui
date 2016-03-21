package net.dv8tion.discord.commands;

import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Austin on 3/21/2016.
 */
public class InfoCommand extends Command

{
    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        MessageBuilder builder = new MessageBuilder();
        builder.appendString("__Yui Information__\n")
                .appendString("__Creator__\n")
                .appendString("    **Name**:          DV8FromTheWorld (#6297)\n")
                .appendString("    **ID**:                107562988810027008\n")
                .appendString("    **Github**:        <http://code.dv8tion.net>\n")
                .appendString("__Development__\n")
                .appendString("    **Language**:   Java 8\n")
                .appendString("    **Library**:        JDA - v" + JDAInfo.VERSION + "\n")
                .appendString("    **Source**:        <https://github.com/DV8FromTheWorld/Yui>");
        sendMessage(e, builder.build());
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".info");
    }

    @Override
    public String getDescription()
    {
        return "Provides information about Yui.";
    }

    @Override
    public String getName()
    {
        return "Yui Information";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Arrays.asList(".info - Prints all information pertaining to the current instance of Yui.");
    }
}
