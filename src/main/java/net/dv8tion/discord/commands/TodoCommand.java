package net.dv8tion.discord.commands;

import net.dv8tion.discord.util.Database;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import javax.xml.crypto.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
                    todoList.allowedUsers.add(sqlTodoEntries.getString(1)); //UserId
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
        catch (SQLException e1)
        {
            sendMessage(e, "An SQL error occured while processing command.\nError Message: " + e1.getMessage());
            e1.printStackTrace();
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

    //alias show [ListName]
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
    private void handleAdd(MessageReceivedEvent e, String[] args) throws SQLException
    {
        String label = args[2].toLowerCase();
        String content = StringUtils.join(args, " ", 3, args.length);

        TodoList todoList = todoLists.get(label);
        if (todoList != null)
        {
            if (todoList.locked && todoList.isAuthUser(e.getAuthor()))
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
        else
        {
            PreparedStatement addTodoList = Database.getInstance().getStatement(ADD_TODO_LIST);
            addTodoList.setString(1, label);                //Label
            addTodoList.setString(2, e.getAuthor().getId());//OwnerId
            addTodoList.setBoolean(3, false);               //Locked
            if (addTodoList.executeUpdate() == 0)
                throw new SQLException(ADD_TODO_LIST + " reported no modified rows!");

            todoList = new TodoList(Database.getAutoIncrement(addTodoList, 1), label, e.getAuthor().getId(), false);
            todoLists.put(label, todoList);
            addTodoList.clearParameters();

            PreparedStatement addTodoEntry = Database.getInstance().getStatement(ADD_TODO_ENTRY);
            addTodoEntry.setInt(1, todoList.id);
            addTodoEntry.setString(2, content);
            addTodoEntry.setBoolean(3, false);
            if (addTodoEntry.executeUpdate() == 0)
                throw new SQLException(ADD_TODO_ENTRY + " reported no modified rows!");

            todoList.entries.add(new TodoEntry(Database.getAutoIncrement(addTodoEntry, 1), content, false));
            addTodoEntry.clearParameters();

            sendMessage(e, "Created `" + label + "` todo list. Provided content added as first entry.");
        }
    }

    //alias check [ListName] [EntryIndex]
    //alias mark [ListName] [EntryIndex]
    //alias uncheck [ListName] [EntryIndex]
    //alias unmark [ListName] [EntryIndex]
    private void handleCheck(MessageReceivedEvent e, String[] args, boolean completed) throws SQLException
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
                builder.appendString("__Owner of `" + label + "`__\n");
                User owner = api.getUserById(todoList.ownerId);
                if (owner != null)
                    builder.appendString(" - " + owner.getUsername());
                else
                    builder.appendString(" - Unknown User ID: " + todoList.ownerId);
                builder.appendString("\n");
                builder.appendString("__Other Auth'd Users__\n");

                for (String id : todoList.allowedUsers)
                {
                    User u = api.getUserById(id);
                    if (u != null)
                        builder.appendString(" - " + u.getUsername());
                    else
                        builder.appendString(" - Unknown User ID: " + id);
                    builder.appendString("\n");
                }
                if (todoList.allowedUsers.isEmpty())
                    builder.appendString(" - None.");
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
