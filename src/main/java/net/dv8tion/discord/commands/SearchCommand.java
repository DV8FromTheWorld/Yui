/**
 *     Copyright 2015-2016 Austin Keener
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.discord.commands;

import net.dv8tion.discord.util.GoogleSearch;
import net.dv8tion.discord.util.SearchResult;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

		List<SearchResult> results = GoogleSearch.performSearch(
				"018291224751151548851%3Ajzifriqvl1o",
				StringUtils.join(args, "+", 1, args.length)
						+ ((filter != null) ? ("+" + filter) : ""));

		sendMessage(e, results.get(0).getSuggestedReturn());
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
				".google *<search terms>*  **OR** .wiki *<search terms>* **OR** .urban *<search terms>*\n");
    }
}