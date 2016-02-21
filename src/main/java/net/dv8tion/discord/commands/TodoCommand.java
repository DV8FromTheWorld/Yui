package net.dv8tion.discord.commands;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class TodoCommand extends Command
{
    private JDA api;
    private HashMap<String, TodoList> todoLists = new HashMap<>();

    public TodoCommand(JDA api)
    {
        this.api = api;
    }

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        switch (args[1].toLowerCase())
        {
            case "show":
                handleShow(e, args);
                break;
            case "add":
                handleAdd(e, args);
                break;
            case "mark":
            case "check":
                handleCheck(e, args, true);
                break;
            case "unmark":
            case "uncheck":
                handleCheck(e, args, false);
                break;
            case "lock":
                handleLock(e, args, true);
                break;
            case "unlock":
                handleLock(e, args, false);
                break;
            case "users":
                handleUsers(e, args);
                break;
            case "clear":
                handleClear(e, args);
                break;
            case "remove":
                handleRemove(e, args);
                break;
        }
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".todo");
    }

    @Override
    public String getDescription()
    {
        return "Used to create todo lists that can be checked off as things are completed.";
    }

    @Override
    public String getName()
    {
        return "Todo Command";
    }

    @Override
    public String getUsageInstructions()
    {
        return null;
    }

    //alias show [ListName]s
    private void handleShow(MessageReceivedEvent e, String[] args)
    {
        String label = args[2].toLowerCase();
        TodoList todoList = todoLists.get(label);
        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list.");
            return;
        }

        // Discord messages can only be 2000 characters.
        List<Message> todoMessages = new ArrayList<Message>();
        MessageBuilder builder = new MessageBuilder();
        builder.appendString("__Todo for: `" + label + "`__\n");
        for (int i = 0; i < todoList.entries.size(); i++)
        {
            TodoEntry todoEntry = todoList.entries.get(i);
            String todoEntryString = todoEntry.content;
            if (todoEntry.checked)
                todoEntryString = "~~" + todoEntryString + "~~";
            todoEntryString = (i + 1) + ") " + todoEntryString + "\n";
            if (builder.getLength() + todoEntryString.length() > 2000)
            {
                todoMessages.add(builder.build());
                builder = new MessageBuilder();
            }
            builder.appendString(todoEntryString);
        }

        todoMessages.forEach(message -> sendMessage(e, message));
        sendMessage(e, builder.build());
    }

    //alias add [ListName] [Content ... ]
    private void handleAdd(MessageReceivedEvent e, String[] args)
    {
        String label = args[2].toLowerCase();
        String content = StringUtils.join(args, " ", 3, args.length);

        TodoList todoList = todoLists.get(label);
        if (todoList != null)
        {
            if (todoList.locked && todoList.allowedUsers.stream().anyMatch(id -> e.getAuthor().getId().equals(id)))
            {
                sendMessage(e, "Sorry, `" + label + "` is a locked todo list and you do not have permission to modify it.");
                return;
            }

            //TODO: interface with DB. add the new todo entry. add the new corresponding boolean value.

            //TODO: replace with DB id.
            todoList.entries.add(new TodoEntry(todoList.entries.size() + 1, content));

            sendMessage(e, "Added to `" + label + "` todo list.");
        }
        else
        {
            //TODO: interface with DB. Create new TodoList, get ID
            //TODO: create new TodoEntry, getID, connect with the new TodoList.
            //TODO: create new allowed user

            //TODO: replace ids with DB id.
            TodoList newTodoList = new TodoList(todoLists.size() + 1, label);
            todoLists.put(label, newTodoList);

            TodoEntry newTodoEntry = new TodoEntry(1, content);
            newTodoList.entries.add(newTodoEntry);

            newTodoList.allowedUsers.add(e.getAuthor().getId());

            sendMessage(e, "Created `" + label + "` todo list. Provided content added as first entry.");
        }
    }

    //alias check [ListName] [EntryIndex]
    //alias mark [ListName] [EntryIndex]
    //alias uncheck [ListName] [EntryIndex]
    //alias unmark [ListName] [EntryIndex]
    private void handleCheck(MessageReceivedEvent e, String[] args, boolean completed)
    {
        String label = args[2].toLowerCase();
        TodoList todoList = todoLists.get(label);
        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list.");
            return;
        }

        String todoEntryString = args[3];

        if (todoEntryString.equals("*"))
        {
            todoList.entries.forEach(todoEntry ->
            {
                if (!todoEntry.checked)
                {
                    //TODO: interface with database, set checked true.

                    todoEntry.checked = true;
                }
            });
            sendMessage(e, "Set all entries in the `" + label + "` todo list to **completed**.");
        }
        else
        {
            int todoEntry;
            try
            {
                //We subtract 1 from the provided value because entries are listed from 1 and higher.
                // People don't start counting from 0, so when we display the list of entries, we start from.
                // This means that the entry index they enter will actually be 1 greater than the actual entry.
                todoEntry = Integer.parseInt(todoEntryString) - 1;
            }
            catch (NumberFormatException ex)
            {
                sendMessage(e, "The provided value as an index to mark was not a number. Value provided: `" + todoEntryString + "`");
                return;
            }

            if (todoEntry < 0 || todoEntry  + 1 > todoList.entries.size())
            {
                //We add 1 back to the todoEntry because we subtracted 1 from it above. (Basically, we make it human readable again)
                sendMessage(e, "The provided index to mark does not exist in this Todo list. Value provided: `" + (todoEntry + 1) + "`");
                return;
            }

            //TODO: interface with database, modify checked value.

            todoList.entries.get(todoEntry).checked = completed;

            sendMessage(e, "Item `" + (todoEntry + 1) + "` in `" + label + "` was marked as **" + (completed ? "completed**" : "incomplete**") );
        }
    }

    //alias lock [ListName]
    private void handleLock(MessageReceivedEvent e, String[] args, boolean lock)
    {
        String label = args[2].toLowerCase();
        TodoList todoList = todoLists.get(label);
        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list.");
            return;
        }

        if (!todoList.allowedUsers.stream().anyMatch(id -> e.getAuthor().getId().equals(id)))
        {
            sendMessage(e, "Sorry, you do not have permission to lock or unlock the `" + label + "` todo list.");
            return;
        }

        //TODO: Interact with DB.

        todoList.locked = lock;
        sendMessage(e, "The `" + label + "` todo list was `" + (lock ? "locked`" : "unlocked`"));
    }

    //alias users add [ListName] @mention @mention ...
    //alias users remove [ListName] @mention @mention ...
    //alias users list [ListName]
    private void handleUsers(MessageReceivedEvent e, String[] args)
    {
        String action = args[2].toLowerCase();
        String label = args[3].toLowerCase();
        TodoList todoList = todoLists.get(label);
        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list.");
            return;
        }

        switch (action)
        {
            case "add":
            {
                if (!todoList.allowedUsers.stream().anyMatch(id -> e.getAuthor().getId().equals(id)))
                {
                    sendMessage(e, "Sorry, you don't have permission add users to the `" + label + "` todo list.");
                    return;
                }

                if (e.getMessage().getMentionedUsers().size() == 0)
                {
                    sendMessage(e, "No users were specified to add to the `" + label + "` todo list.");
                    return;
                }

                int addedUsers = 0;
                for (User u : e.getMessage().getMentionedUsers())
                {
                    if (!todoList.allowedUsers.stream().anyMatch(id -> u.getId().equals(id)))
                    {
                        //TODO: interface with DB

                        todoList.allowedUsers.add(u.getId());
                        addedUsers++;
                    }
                }

                sendMessage(e, "Added **" + addedUsers + "** users to the `" + label + "` todo list.");
                break;
            }
            case "remove":
            {
                if (!todoList.allowedUsers.stream().anyMatch(id -> e.getAuthor().getId().equals(id)))
                {
                    sendMessage(e, "Sorry, you don't have permission add users to the `" + label + "` todo list.");
                    return;
                }

                if (e.getMessage().getMentionedUsers().size() == 0)
                {
                    sendMessage(e, "No users were specified to add to the `" + label + "` todo list.");
                    return;
                }

                int removedUsers = 0;
                for (User u : e.getMessage().getMentionedUsers())
                {
                    if (!todoList.allowedUsers.stream().anyMatch(id -> u.getId().equals(id)))
                    {
                        //TODO: interface with DB

                        todoList.allowedUsers.remove(u.getId());
                        removedUsers++;
                    }
                }

                sendMessage(e, "Removed **" + removedUsers + "** users from the `" + label + "` todo list.");
                break;
            }
            case "list":
            {
                MessageBuilder builder = new MessageBuilder();
                builder.appendString("__Users for: `" + label + "`\n");
                for (String id : todoList.allowedUsers)
                {
                    User u = api.getUserById(id);
                    if (u != null)
                        builder.appendString(u.getUsername());
                    else
                        builder.appendString("Unknown User ID: " + id);
                    builder.appendString("\n");
                }
                sendMessage(e ,builder.build());
                break;
            }
            default:
            {
                sendMessage(e, "Sorry, the provided sub-action argument for the `users` action is not recognized. " +
                        "Provided argument: `" + action + "`");
                return;
            }
        }
    }

    //alias clear [ListName]
    public void handleClear(MessageReceivedEvent e, String[] args)
    {
        String label = args[2];
        TodoList todoList = todoLists.get(label);
        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list.");
            return;
        }

        if (todoList.locked && !todoList.allowedUsers.stream().anyMatch(id -> e.getAuthor().getId().equals(id)))
        {
            sendMessage(e, "Sorry, the `" + label +"` todo list is locked and you do not have permission to modify it.");
            return;
        }

        int clearedEntries = 0;
        for (Iterator<TodoEntry> it = todoList.entries.iterator(); it.hasNext();)
        {
            TodoEntry todoEntry = it.next();
            if (todoEntry.checked)
            {
                //TODO: interface with DB.

                it.remove();
                clearedEntries++;
            }
        }
        sendMessage(e, "Cleared **" + clearedEntries + "** completed entries from the `" + label + "` todo list.");
    }

    //alias remove [ListName]
    public void handleRemove(MessageReceivedEvent e, String[] args)
    {
        String label = args[2].toLowerCase();
        TodoList todoList = todoLists.get(label);
        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list.");
            return;
        }

        if (todoList.locked && !todoList.allowedUsers.stream().anyMatch(id -> e.getAuthor().getId().equals(id)))
        {
            sendMessage(e, "Sorry, the `" + label +"` todo list is locked and you do not have permission to modify it.");
            return;
        }

        //TODO: interface with DB.

        todoLists.remove(label);
        sendMessage(e, "Deleted the `" + label + "` todo list.");
    }

    private static class TodoList
    {
        int id;
        String labelName;
        boolean locked;
        List<TodoEntry> entries;
        List<String> allowedUsers;

        TodoList(int id, String labelName)
        {
            this.id = id;
            this.labelName = labelName;
            this.locked = false;
            this.entries = new ArrayList<>();
            this.allowedUsers = new ArrayList<>();
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof TodoList))
                return false;

            TodoList tl = (TodoList) o;
            return tl.id == this.id && tl.labelName.equals(this.labelName) && tl.locked == this.locked;
        }

        @Override
        public int hashCode()
        {
            return toString().hashCode();
        }

        @Override
        public String toString()
        {
            return "TodoLabel: Id: " + id + " Name: " + labelName + " Size: " + entries.size() + " Locked: " + locked;
        }
    }

    private static class TodoEntry
    {
        int id;
        String content;
        boolean checked;

        TodoEntry(int id, String content)
        {
            this.id = id;
            this.content = content;
            this.checked = false;
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof TodoEntry))
                return false;

            TodoEntry te = (TodoEntry) o;
            return te.id == this.id && te.content.equals(this.content) && te.checked == this.checked;
        }

        @Override
        public int hashCode()
        {
            return toString().hashCode();
        }

        @Override
        public String toString()
        {
            return "TodoEntry: Id: " + id + " Checked: " + checked + " Content: " + content;
        }
    }
}
