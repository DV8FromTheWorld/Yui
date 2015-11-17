package net.dv8tion.discord.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;
import net.dv8tion.discord.Downloader;

public class PullCommand extends Command
{
    private String gitRepoUrl;
    private String javaJDKPath;

    public PullCommand(String gitRepoUrl, String javaJDKPath)
    {
        this.gitRepoUrl = gitRepoUrl;
        this.javaJDKPath = javaJDKPath;

        if (!gitRepoUrl.endsWith("/"))
            this.gitRepoUrl += "/archive/master.zip";
        else
            this.gitRepoUrl +="archive/master.zip";
    }

    @Override
    public void onChat(UserChatEvent e)
    {
        String[] args = e.getMsg().toString().split(" ");
        if (!aliases().contains(args[0]))
        {
            return;
        }

        //TODO: Check permission - admin
        try
        {
            Downloader.file(gitRepoUrl, "./source/Master.zip");
        }
        catch (IOException e1)
        {
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(String.format(": Error attempt to pull.\n**%s**: %s\nPlease check the console for stacktrace.",
                        e1.getClass().getName(),
                        e1.getMessage()))
                .build());
            e1.printStackTrace();
        }
    }

    @Override
    public List<String> aliases()
    {
        return Arrays.asList(new String[] {".pull", ".pull-reload"});
    }

    @Override
    public String commandDescription()
    {
        return "Used to pull source code from the Bot's git repo and compile it";
    }

    @Override
    public String helpMessage()
    {
        return null;
    }

}
