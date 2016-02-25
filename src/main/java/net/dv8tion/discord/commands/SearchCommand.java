package net.dv8tion.discord.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.dv8tion.discord.util.GoogleSearch;

import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class SearchCommand extends Command
{
	@Override
	public void onCommand(MessageReceivedEvent e, String[] args)
	{
		String filter = null;
		switch (args[0]) {
			case ".google":
			case ".g":
				break;
			case ".wiki":
				filter = "wiki";
				break;
			case ".urban":
				filter = "site:urbandictionary.com";
				break;
			default:
				return;
		}

		GoogleSearch search = new GoogleSearch(
				StringUtils.join(args, "+", 1, args.length)
						+ ((filter != null) ? ("+" + filter) : ""));

		sendMessage(e, search.getSuggestedReturn());
	}

	@Override
    public List<String> getAliases()
    {
        return Arrays.asList(".google", ".g", ".wiki", ".urban");
    }

    @Override
    public String getDescription()
    {
        return "Allows you to search Google. The aliases search Wiki sites (not wikipedia exclusive) and Urban Dictionary.";
    }

    @Override
    public String getName()
    {
        return "Google Search";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
				".google *<search terms>  **OR** .wiki *<search terms>*  **OR**  .urban *<search terms>*\n");
    }
}