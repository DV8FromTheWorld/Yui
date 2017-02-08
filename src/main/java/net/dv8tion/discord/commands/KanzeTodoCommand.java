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

import net.dv8tion.discord.Permissions;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KanzeTodoCommand extends Command
{
    private static final String TODO_CHANNEL_ID = "150615429369430017";
    private static final String JDA_GUILD_ID = "125227483518861312";
    private File jsonFile;
    private JSONArray todoList;
    private JDA api;

    public KanzeTodoCommand(JDA api)
    {
        this.api = api;
        try
        {
            jsonFile = new File("todo-list.json");
            if (jsonFile.exists())
            {
                todoList = new JSONArray(new String(Files.readAllBytes(jsonFile.toPath())));
            }
            else
            {
                todoList = new JSONArray();
                Files.write(jsonFile.toPath(), todoList.toString(4).getBytes());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        if (!e.isFromType(ChannelType.TEXT) || !e.getGuild().getId().equals(JDA_GUILD_ID))
            return;

        if (!Permissions.getPermissions().isOp(e.getAuthor()))
            return;

        if (args.length < 2)
            return;

        boolean success = false;
        if (args[1].equals("clear"))
            success = clearTodo();
        else if (args[1].startsWith("-"))
            success = markTodo(args[1], e);
        else
            success = addTodo(StringUtils.join(args, " ", 1, args.length), e);

        if (success)
            e.getMessage().deleteMessage().queue();
    }

    private boolean addTodo(String todoMessage, MessageReceivedEvent e)
    {
        if (todoMessage.length() > 350)
        {
            e.getTextChannel().sendMessage(e.getMember().getEffectiveName() + ": Provided todo entry was greater than 350 characters. Be more concise!").queue();
            return false;
        }

        JSONObject section = addToList(todoMessage, false);

        //Since index represents the latest index, we can determine where the first
        // index of this section starts by subtracting the size of
        // the section from the index.
        sendSectionToDiscord(section, getCurrentIndex() - (section.getJSONArray("entries").length() - 1));
        updateSaveFile();
        return true;
    }

    private boolean markTodo(String arg, MessageReceivedEvent e)
    {
        arg = arg.substring(1, arg.length());
        if (arg.isEmpty())
        {
            e.getTextChannel().sendMessage(e.getMember().getEffectiveName() + ": No number was provided to mark! Example: `!!todo -3`").queue();
            return false;
        }
        int index;
        try
        {
            index = Integer.valueOf(arg);
        }
        catch (NumberFormatException ex)
        {
            e.getTextChannel().sendMessage(e.getMember().getEffectiveName() + ": Incorrect number format. You provided: `" + arg + "`. This isn't an int").queue();
            return false;
        }

        if (index < 1 || index > getCurrentIndex())
        {
            e.getTextChannel().sendMessage(e.getMember().getEffectiveName() + ": Provided a non existent index: `" + index + "`").queue();
            return false;
        }

        int searchIndex = 1;

        listLoop:
        for (int i = 0; i < todoList.length(); i++)
        {
            JSONObject section = todoList.getJSONObject(i);
            JSONArray entries = section.getJSONArray("entries");
            for (int j = 0; j < entries.length(); j++)
            {
                if (index == searchIndex)
                {
                    JSONObject entry = entries.getJSONObject(j);
                    entry.put("marked", !entry.getBoolean("marked"));
                    sendSectionToDiscord(section, index - j);
                    break listLoop;
                }
                searchIndex++;
            }
        }
        updateSaveFile();
        return true;
    }
    private boolean clearTodo()
    {
        if (todoList.length() == 0)
            return true;
        List<JSONObject> savedEntries = new ArrayList<>();
        List<String> oldMessageIds = new ArrayList<>();
        for (int i = 0; i < todoList.length(); i++)
        {
            JSONObject section = todoList.getJSONObject(i);
            JSONArray entries = section.getJSONArray("entries");
            oldMessageIds.add(section.getString("messageId"));
            for (int j = 0; j < entries.length(); j++)
            {
                JSONObject entry = entries.getJSONObject(j);
                if (!entry.getBoolean("marked"))
                    savedEntries.add(entry);
            }
        }

        TextChannel channel = api.getTextChannelById(TODO_CHANNEL_ID);
        oldMessageIds.forEach(id -> channel.deleteMessageById(id).queue());

        todoList = new JSONArray();
        for (JSONObject entry : savedEntries)
        {
            addToList(entry.getString("content"), false);
        }
        int index = 1;
        for (int i = 0; i < todoList.length(); i++)
        {
            JSONObject section = todoList.getJSONObject(i);
            sendSectionToDiscord(section, index);
            index += section.getJSONArray("entries").length();
        }
        updateSaveFile();
        return true;
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList("!!todo");
    }

    @Override
    public String getDescription()
    {
        return "Todo command implemented by Kantenkugel's KanzeBot. This command only works for Bot-OPs in the JDA guild.";
    }

    @Override
    public String getName()
    {
        return "JDA Todo command";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Arrays.asList("`!!todo <message>` to add a todo entry.\n" +
                "`!!todo -#`  to mark an entry as complete.\n" +
                "`!!todo clear` to clear all completed todo entries.");
    }

    private JSONObject addToList(String todoMessage, boolean marked)
    {
        JSONObject section;
        if (todoList.length() == 0)
        {
            section = createNewSection();
            todoList.put(section);
        }
        else
        {
            section = todoList.getJSONObject(todoList.length() - 1);
        }

        //we use 1900 instead of the message cap of 2000 just to make sure
        // we don't go over it with stuff like formatting.
        //Also only want a max of 10 entries.
        if (section.getJSONArray("entries").length() > 9
                || section.getInt("length") + todoMessage.length() > 1900)
        {
            section = createNewSection();
            todoList.put(section);
        }

        JSONObject entry = createNewEntry(todoMessage, marked);
        section.getJSONArray("entries").put(entry);
        section.put("length", section.getInt("length") + todoMessage.length());
        return section;
    }

    private JSONObject createNewSection()
    {
        return new JSONObject()
                .put("messageId", JSONObject.NULL)
                .put("length", 0)
                .put("entries", new JSONArray());
    }

    private JSONObject createNewEntry(String content, boolean marked)
    {
        return new JSONObject()
                .put("content", content)
                .put("marked", marked);
    }

    private void updateSaveFile()
    {
        synchronized (todoList)
        {
            try
            {
                Files.write(jsonFile.toPath(), todoList.toString(4).getBytes());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private int getCurrentIndex()
    {
        int index = 0;
        for (Object obj : todoList)
        {
            JSONObject section = (JSONObject) obj;
            index += section.getJSONArray("entries").length();
        }
        return index;
    }

    private void sendSectionToDiscord(JSONObject section, int beginIndex)
    {
        StringBuilder sb = new StringBuilder();
        JSONArray entries = section.getJSONArray("entries");
        for (int i = 0; i < entries.length(); i++)
        {
            JSONObject entry = entries.getJSONObject(i);
            String content = entry.getString("content");
            boolean marked = entry.getBoolean("marked");

            sb.append((beginIndex + i) + ") ");
            if (marked)
                sb.append("~~").append(content).append("~~");
            else
                sb.append(content);
            sb.append("\n");
        }
        if (section.isNull("messageId"))
        {
            api.getTextChannelById(TODO_CHANNEL_ID).sendMessage(sb.toString()).queue(msg ->
            {
                section.put("messageId", msg.getId());
                updateSaveFile();
            });
        }
        else
        {
            api.getTextChannelById(TODO_CHANNEL_ID)
                    .getMessageById(section.getString("messageId")).complete()
                    .editMessage(sb.toString()).queue();
        }
    }
}
