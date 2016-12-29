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

import net.dv8tion.discord.util.Database;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class TodoCommand extends Command
{
    //Database Methods
    public static final String ADD_TODO_LIST = "addTodoList";
    public static final String ADD_TODO_ENTRY = "addTodoEntry";
    public static final String ADD_TODO_USER = "addTodoUser";
    public static final String GET_TODO_LISTS = "getTodoLists";
    public static final String GET_TODO_ENTRIES = "getTodoEntries";
    public static final String GET_TODO_USERS = "getTodoUsers";
    public static final String SET_TODO_LIST_LOCKED = "setTodoListLocked";
    public static final String SET_TODO_ENTRY_CHECKED = "setTodoEntryChecked";
    public static final String SET_TODO_ENTRIES_CHECKED = "setTodoEntriesChecked";
    public static final String REMOVE_TODO_LIST = "removeTodoList";
    public static final String REMOVE_TODO_ENTRY = "removeTodoEntry";
    public static final String REMOVE_TODO_USER = "removeTodoUser";

    private JDA api;
    private HashMap<String, TodoList> todoLists = new HashMap<>();

    public TodoCommand(JDA api)
    {
        this.api = api;
        try
        {
            ResultSet sqlTodoLists = Database.getInstance().getStatement(GET_TODO_LISTS).executeQuery();
            while (sqlTodoLists.next())
            {
                String label = sqlTodoLists.getString(2);
                TodoList todoList = new TodoList(
                        sqlTodoLists.getInt(1),     //Id
                        label,
                        sqlTodoLists.getString(3),  //OwnerId
                        sqlTodoLists.getBoolean(4)  //Locked
                );
                todoLists.put(label, todoList);

                PreparedStatement getEntries = Database.getInstance().getStatement(GET_TODO_ENTRIES);
                getEntries.setInt(1, todoList.id);
                ResultSet sqlTodoEntries = getEntries.executeQuery();
                while (sqlTodoEntries.next())
                {
                    TodoEntry todoEntry = new TodoEntry(
                            sqlTodoEntries.getInt(1),       //Id
                            sqlTodoEntries.getString(2),    //Content
                            sqlTodoEntries.getBoolean(3)    //Checked
                    );
                    todoList.entries.add(todoEntry);
                }
                getEntries.clearParameters();

                PreparedStatement getUsers = Database.getInstance().getStatement(GET_TODO_USERS);
                getUsers.setInt(1, todoList.id);
                ResultSet sqlTodoUsers = getUsers.executeQuery();
                while(sqlTodoUsers.next())
                {
                    todoList.allowedUsers.add(sqlTodoUsers.getString(1)); //UserId
                }
                getUsers.clearParameters();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        try
        {
            checkArgs(args, 1, "No Action argument was provided. Please use `.help "+ getAliases().get(0) + "` for more information.");

            switch (args[1].toLowerCase())
            {
                case "show":
                    handleShow(e, args);
                    break;
                case "lists":
                    handleLists(e, args);
                    break;
                case "create":
                    handleCreate(e, args);
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
                default:
                    sendMessage(e, "Unknown Action argument: `" + args[1] + "` was provided. " +
                            "Please use `.help "+ getAliases().get(0) + "` for more information.");
            }
        }
        catch (SQLException e1)
        {
            sendMessage(e, "An SQL error occured while processing command.\nError Message: " + e1.getMessage());
            e1.printStackTrace();
        }
        catch (IllegalArgumentException e2)
        {
            sendMessage(e, e2.getMessage());
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
    public List<String> getUsageInstructions()
    {
        return Arrays.asList(
                String.format(
                "%1$s [Action] <Action Arguments>\n" +
                "__Actions:__\n" +
                "\n" +
                "__**show [ListName]** - Shows all todo entries in the [ListName] TodoList.__\n" +
                "       Example: `%1$s show shopping-list` would display all entries in the `shopping-list` list.\n" +
                "\n" +
                "__**lists <Mentions...>** - Displays the todo lists owned by the provided user(s).__\n" +
                "       Example 1: `%1$s lists`  Displays lists owned by the User that executed the command.\n" +
                "       Example 2: `%1$s lists @DV8FromTheWorld`  Displays lists owned by DV8FromTheWorld.\n" +
                "\n" +
                "__**create [ListName]** - Creates a new todo list with name [ListName]__\n" +
                "       Example: `%1$s list project5`  would create a todo list with the name `project5`\n" +
                "\n" +
                "__**add [ListName] [Content...]** - Adds a todo entry to the [ListName] todo list.__\n" +
                "       Example: `%1$s add project5 Fix bug where Users can delete System32`\n" +
                "\n" +
                "__**mark/unmark [TodoList] [EntryIndex]** - Marks a todo entry as **complete** or *incomplete**.__\n" +
                "       Example 1: `%1$s mark project5 2` Marks the second entry in the project5 list as compelted.\n" +
                "       Example 2: `%1$s unmark project5 3` Marks the third entry in the project5 list as incomplete.\n" +
                "       Example 3: `%1$s mark project5 *` Marks **all** todo entries in the project5 list as completed.\n" +
                "     **Note:** You can also use `check` and `uncheck`.\n" +
                "\n" +
                "__**lock/unlock [ListName]** - Used to lock a todo list such that only Auth'd users can modify it.__\n" +
                "       Example 1: `%1$s lock project5` Locks the project5 list such that only Auth'd users can use `add`,`mark` and `clear`\n" +
                "       Example 2: `%1$s unlock project5` Unlocks the project5 list so that all users can modify it.\n",
                getAliases().get(0)),

                //Second Usage Message
                String.format(
                "__**users [SubAction] [ListName] <SubAction Args>** Used add, remove and list the Auth'd users for a todo list.__\n" +
                "     __SubActions__:\n" +
                "\n" +
                "       __**add [ListName] [@mentions...]** Adds the mentions users to the Auth'd users for ListName list.__\n" +
                "           Example: `%1$s users add project5 @Joe @DudeMan` Adds Joe and DudeMan Auth'd users for the project5 list.\n" +
                "       __**remove [ListName] [@mentions...]** Removes the mentioned users from the Auth'd users for ListName list.__\n" +
                "           Example: `%1$s users remove project5 @MrCatMan` Removes MrCatMan from the Auth'd users for the project5 list.\n" +
                "       __**list [ListName]** Lists the Owner and Auth'd users for the ListName list.__\n" +
                "           Example: `%1$s users list project5` Lists the owner and all Auth'd users for the project5 list.\n" +
                "\n" +
                "__**clear [ListName]** - Clears all **completed** todo entries from a list.__\n" +
                "       Example: `%1$s clear project5` Clears all **completed** todo entries in the project5 list\n" +
                "\n" +
                "__**remove [ListName]** - Completely deletes the ListName list. Only the list owner can do this.__\n" +
                "       Example: `%1$s remove project5` Completely deletes the project5 todo list.\n",
                getAliases().get(0)));
    }

    //alias show [ListName]
    private void handleShow(MessageReceivedEvent e, String[] args)
    {
        checkArgs(args, 2, "No todo ListName was specified. Usage: `" + getAliases().get(0) + " show [ListName]`");

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
        builder.append("__Todo for: `" + label + "`__\n");
        for (int i = 0; i < todoList.entries.size(); i++)
        {
            TodoEntry todoEntry = todoList.entries.get(i);
            String todoEntryString = todoEntry.content;
            if (todoEntry.checked)
                todoEntryString = "~~" + todoEntryString + "~~";
            todoEntryString = (i + 1) + ") " + todoEntryString + "\n";
            if (builder.length() + todoEntryString.length() > 2000)
            {
                todoMessages.add(builder.build());
                builder = new MessageBuilder();
            }
            builder.append(todoEntryString);
        }

        todoMessages.forEach(message -> sendMessage(e, message));
        sendMessage(e, builder.build());
    }

    //alias lists
    //alias lists [mentions...]
    private void handleLists(MessageReceivedEvent e, String[] args)
    {
        List<User> mentionedUsers = e.getMessage().getMentionedUsers();
        if (mentionedUsers.size() == 0)
            mentionedUsers = Collections.singletonList(e.getAuthor());

        List<Message> messages = new LinkedList<Message>();
        for (User u : mentionedUsers)
        {
            MessageBuilder builder = new MessageBuilder();
            List<TodoList> lists = todoLists.values().stream().filter(list -> list.ownerId.equals(u.getId())).collect(Collectors.toList());
            builder.append("__" + u.getName() + " owns **" + lists.size() + "** todo lists.__\n");
            for (TodoList list : lists)
            {
                String listString = " - " + list.labelName + "\n";
                if (builder.length() + listString.length() > 2000)
                {
                    messages.add(builder.build());
                    builder = new MessageBuilder();
                }
                builder.append(listString);
            }
            messages.add(builder.build());
        }

        messages.forEach(msg -> sendMessage(e, msg));
    }

    //alias create [ListName]
    private void handleCreate(MessageReceivedEvent e, String[] args) throws SQLException
    {
        checkArgs(args, 2, "No ListName for the new todo list was provided. Usage: `" + getAliases().get(0) + " create [ListName]`");

        String label = args[2].toLowerCase();
        TodoList todoList = todoLists.get(label);

        if (todoList != null)
        {
            sendMessage(e, "A todo list already exists with the name `" + label + "`.");
            return;
        }

        PreparedStatement addTodoList = Database.getInstance().getStatement(ADD_TODO_LIST);
        addTodoList.setString(1,  label);                //Label
        addTodoList.setString(2, e.getAuthor().getId());//OwnerId
        addTodoList.setBoolean(3, false);               //Locked
        if (addTodoList.executeUpdate() == 0)
            throw new SQLException(ADD_TODO_LIST + " reported no modified rows!");

        todoList = new TodoList(Database.getAutoIncrement(addTodoList, 1), label, e.getAuthor().getId(), false);
        todoLists.put(label, todoList);
        addTodoList.clearParameters();

        sendMessage(e, "Created `" + label + "` todo list. Use `" + getAliases().get(0) + " add " + label + " [content...]` " +
                "to add entries to this todo list.");
    }

    //alias add [ListName] [Content ... ]
    private void handleAdd(MessageReceivedEvent e, String[] args) throws SQLException
    {
        checkArgs(args, 2, "No todo ListName was specified. Usage: `" + getAliases().get(0) + " add [ListName] [content...]`");
        checkArgs(args, 3, "No content was specified. Cannot create an empty todo entry!" +
                "Usage: `" + getAliases().get(0) + " add [ListName] [content...]`");

        String label = args[2].toLowerCase();
        String content = StringUtils.join(args, " ", 3, args.length);
        TodoList todoList = todoLists.get(label);

        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list. " +
                    "Try using `" + getAliases().get(0) + " create " + label + "` to create a new list by this name.");
            return;
        }

        if (todoList.locked && !todoList.isAuthUser(e.getAuthor()))
        {
            sendMessage(e, "Sorry, `" + label + "` is a locked todo list and you do not have permission to modify it.");
            return;
        }

        PreparedStatement addTodoEntry = Database.getInstance().getStatement(ADD_TODO_ENTRY);
        addTodoEntry.setInt(1, todoList.id);
        addTodoEntry.setString(2, content);
        addTodoEntry.setBoolean(3, false);
        if (addTodoEntry.executeUpdate() == 0)
            throw new SQLException(ADD_TODO_ENTRY + " reported no modified rows!");

        todoList.entries.add(new TodoEntry(Database.getAutoIncrement(addTodoEntry, 1), content, false));
        addTodoEntry.clearParameters();

        sendMessage(e, "Added to `" + label + "` todo list.");
    }

    //alias check [ListName] [EntryIndex]
    //alias mark [ListName] [EntryIndex]
    //alias uncheck [ListName] [EntryIndex]
    //alias unmark [ListName] [EntryIndex]
    private void handleCheck(MessageReceivedEvent e, String[] args, boolean completed) throws SQLException
    {
        checkArgs(args, 2, "No todo ListName was specified. Usage: `" + getAliases().get(0) + " mark/unmark [ListName] [EntryIndex]`");
        checkArgs(args, 3, "No todo EntryIndex was specified. Usage: `" + getAliases().get(0) + " mark/unmark [ListName] [EntryIndex]`");


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
            PreparedStatement setTodoEntryChecked = Database.getInstance().getStatement(SET_TODO_ENTRIES_CHECKED);
            setTodoEntryChecked.setBoolean(1, completed);
            setTodoEntryChecked.setInt(2, todoList.id);
            if (setTodoEntryChecked.executeUpdate() == 0)
                throw new SQLException(SET_TODO_ENTRIES_CHECKED + " reported no updated rows!");

            todoList.entries.forEach(todoEntry -> todoEntry.checked = completed);

            sendMessage(e, "Set all entries in the `" + label + "` todo list to **" + (completed ? "complete**" : "incomplete**"));
        }
        else
        {
            int todoEntryIndex;
            try
            {
                //We subtract 1 from the provided value because entries are listed from 1 and higher.
                // People don't start counting from 0, so when we display the list of entries, we start from.
                // This means that the entry index they enter will actually be 1 greater than the actual entry.
                todoEntryIndex = Integer.parseInt(todoEntryString) - 1;
            }
            catch (NumberFormatException ex)
            {
                sendMessage(e, "The provided value as an index to mark was not a number. Value provided: `" + todoEntryString + "`");
                return;
            }

            if (todoEntryIndex < 0 || todoEntryIndex  + 1 > todoList.entries.size())
            {
                //We add 1 back to the todoEntry because we subtracted 1 from it above. (Basically, we make it human readable again)
                sendMessage(e, "The provided index to mark does not exist in this Todo list. Value provided: `" + (todoEntryIndex + 1) + "`");
                return;
            }

            TodoEntry todoEntry = todoList.entries.get(todoEntryIndex);
            if (todoEntry.checked != completed)
            {
                PreparedStatement setTodoEntryChecked = Database.getInstance().getStatement(SET_TODO_ENTRY_CHECKED);
                setTodoEntryChecked.setBoolean(1, completed);
                setTodoEntryChecked.setInt(2, todoEntry.id);
                if (setTodoEntryChecked.executeUpdate() == 0)
                    throw new SQLException(SET_TODO_ENTRY_CHECKED + " reported no updated rows!");

                todoEntry.checked = completed;
            }

            sendMessage(e, "Item `" + (todoEntryIndex + 1) + "` in `" + label + "` was marked as **" + (completed ? "completed**" : "incomplete**") );
        }
    }

    //alias lock [ListName]
    private void handleLock(MessageReceivedEvent e, String[] args, boolean locked) throws SQLException
    {
        checkArgs(args, 2, "No todo ListName was specified. Usage: `" + getAliases().get(0) + " lock/unlock [ListName]`");

        String label = args[2].toLowerCase();
        TodoList todoList = todoLists.get(label);
        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list.");
            return;
        }

        if (!todoList.isAuthUser(e.getAuthor()))
        {
            sendMessage(e, "Sorry, you do not have permission to lock or unlock the `" + label + "` todo list.");
            return;
        }

        PreparedStatement setTodoListLocked = Database.getInstance().getStatement(SET_TODO_LIST_LOCKED);
        setTodoListLocked.setBoolean(1, locked);
        setTodoListLocked.setInt(2, todoList.id);
        if (setTodoListLocked.executeUpdate() == 0)
            throw new SQLException(SET_TODO_LIST_LOCKED + " reported no updated rows!");
        setTodoListLocked.clearParameters();

        todoList.locked = locked;
        sendMessage(e, "The `" + label + "` todo list was `" + (locked ? "locked`" : "unlocked`"));
    }

    //alias users add [ListName] @mention @mention ...
    //alias users remove [ListName] @mention @mention ...
    //alias users list [ListName]
    private void handleUsers(MessageReceivedEvent e, String[] args) throws SQLException
    {
        checkArgs(args, 2, "No SubAction was specified. Usage: `" + getAliases().get(0) + " users [SubAction] [ListName]`");
        checkArgs(args, 3, "No todo ListName was specified. Usage: `" + getAliases().get(0) + " users [SubAction] [ListName]`");

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
                if (!todoList.ownerId.equals(e.getAuthor().getId()))
                {
                    sendMessage(e, "Sorry, but only the Owner of a list has permission add users to a todo list.");
                    return;
                }

                if (e.getMessage().getMentionedUsers().size() == 0)
                {
                    sendMessage(e, "No users were specified to add to the `" + label + "` todo list.");
                    return;
                }

                int addedUsers = 0;
                PreparedStatement addTodoUser = Database.getInstance().getStatement(ADD_TODO_USER);
                for (User u : e.getMessage().getMentionedUsers())
                {
                    if (!todoList.isAuthUser(u))
                    {
                        addTodoUser.setInt(1, todoList.id);
                        addTodoUser.setString(2, u.getId());
                        if (addTodoUser.executeUpdate() == 0)
                            throw new SQLException(ADD_TODO_LIST + " reported no updated rows!");
                        addTodoUser.clearParameters();

                        todoList.allowedUsers.add(u.getId());
                        addedUsers++;
                    }
                }

                sendMessage(e, "Added **" + addedUsers + "** users to the `" + label + "` todo list.");
                break;
            }
            case "remove":
            {
                if (!todoList.ownerId.equals(e.getAuthor().getId()))
                {
                    sendMessage(e, "Sorry, but only the Owner of a list has permission remove users from a todo list.");
                    return;
                }

                if (e.getMessage().getMentionedUsers().size() == 0)
                {
                    sendMessage(e, "No users were specified to add to the `" + label + "` todo list.");
                    return;
                }

                int removedUsers = 0;
                PreparedStatement removeTodoUser = Database.getInstance().getStatement(REMOVE_TODO_USER);
                for (User u : e.getMessage().getMentionedUsers())
                {
                    if (todoList.allowedUsers.stream().anyMatch(id -> u.getId().equals(id)))
                    {
                        removeTodoUser.setInt(1, todoList.id);
                        removeTodoUser.setString(2, u.getId());
                        if (removeTodoUser.executeUpdate() == 0)
                            throw new SQLException(REMOVE_TODO_USER + " reported no updated rows!");
                        removeTodoUser.clearParameters();

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
                builder.append("__Owner of `" + label + "`__\n");
                User owner = api.getUserById(todoList.ownerId);
                if (owner != null)
                    builder.append(" - " + owner.getName());
                else
                    builder.append(" - Unknown User ID: " + todoList.ownerId);
                builder.append("\n");
                builder.append("__Other Auth'd Users__\n");

                for (String id : todoList.allowedUsers)
                {
                    User u = api.getUserById(id);
                    if (u != null)
                        builder.append(" - " + u.getName());
                    else
                        builder.append(" - Unknown User ID: " + id);
                    builder.append("\n");
                }
                if (todoList.allowedUsers.isEmpty())
                    builder.append(" - None.");
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
    public void handleClear(MessageReceivedEvent e, String[] args) throws SQLException
    {
        checkArgs(args, 2, "No todo ListName was specified. Usage: `" + getAliases().get(0) + " clear [ListName]`");

        String label = args[2];
        TodoList todoList = todoLists.get(label);
        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list.");
            return;
        }

        if (todoList.locked && !todoList.isAuthUser(e.getAuthor()))
        {
            sendMessage(e, "Sorry, the `" + label +"` todo list is locked and you do not have permission to modify it.");
            return;
        }

        int clearedEntries = 0;
        PreparedStatement removeTodoEntry = Database.getInstance().getStatement(REMOVE_TODO_ENTRY);
        for (Iterator<TodoEntry> it = todoList.entries.iterator(); it.hasNext();)
        {
            TodoEntry todoEntry = it.next();
            if (todoEntry.checked)
            {
                removeTodoEntry.setInt(1, todoEntry.id);
                if (removeTodoEntry.executeUpdate() == 0)
                    throw new SQLException(REMOVE_TODO_ENTRY + " reported no updated rows!");
                removeTodoEntry.clearParameters();

                it.remove();
                clearedEntries++;
            }
        }
        sendMessage(e, "Cleared **" + clearedEntries + "** completed entries from the `" + label + "` todo list.");
    }

    //alias remove [ListName]
    public void handleRemove(MessageReceivedEvent e, String[] args) throws SQLException
    {
        checkArgs(args, 2, "No todo ListName was specified. Usage: `" + getAliases().get(0) + " remove [ListName]`");

        String label = args[2].toLowerCase();
        TodoList todoList = todoLists.get(label);
        if (todoList == null)
        {
            sendMessage(e, "Sorry, `" + label + "` isn't a known todo list.");
            return;
        }

        if (todoList.locked && !todoList.isAuthUser(e.getAuthor()))
        {
            sendMessage(e, "Sorry, the `" + label +"` todo list is locked and you do not have permission to modify it.");
            return;
        }

        PreparedStatement removeTodoList = Database.getInstance().getStatement(REMOVE_TODO_LIST);
        removeTodoList.setInt(1, todoList.id);
        if (removeTodoList.executeUpdate() == 0)
            throw new SQLException(REMOVE_TODO_LIST + " reported no updated rows!");
        removeTodoList.clearParameters();

        todoLists.remove(label);
        sendMessage(e, "Deleted the `" + label + "` todo list.");
    }

    private void checkArgs(String[] args, int index, String failMessage)
    {
        if (args.length < (index + 1))
            throw new IllegalArgumentException(failMessage);
    }

    private static class TodoList
    {
        int id;
        String labelName;
        String ownerId;
        boolean locked;
        List<TodoEntry> entries;
        List<String> allowedUsers;

        TodoList(int id, String labelName, String ownerId, boolean locked)
        {
            this.id = id;
            this.labelName = labelName;
            this.ownerId = ownerId;
            this.locked = locked;
            this.entries = new ArrayList<>();
            this.allowedUsers = new ArrayList<>();
        }

        public boolean isAuthUser(User user)
        {
            return ownerId.equals(user.getId()) || allowedUsers.stream().anyMatch(id -> id.equals(user.getId()));
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

        TodoEntry(int id, String content, boolean checked)
        {
            this.id = id;
            this.content = content;
            this.checked = checked;
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
