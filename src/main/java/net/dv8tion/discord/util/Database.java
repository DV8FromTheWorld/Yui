package net.dv8tion.discord.util;

import net.dv8tion.discord.commands.TodoCommand;

import java.sql.*;
import java.util.HashMap;

public class Database
{
    private static Database instance;

    private Connection conn;
    private HashMap<String, PreparedStatement> preparedStatements;

    public static Database getInstance()
    {
        if (instance == null)
            instance = new Database();
        return instance;
    }

    private Database()
    {
        preparedStatements = new HashMap<String, PreparedStatement>();
        try
        {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:yui.db");
            Statement statement = conn.createStatement();
            statement.setQueryTimeout(30);
            statement.execute("PRAGMA foreign_keys = ON");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Ops(id)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " +
                    "TodoLists(" +
                        "id INTEGER," +
                        "label VARCHAR(50) NOT NULL," +
                        "owner VARCHAR(18) NOT NULL," +
                        "locked BOOLEAN," +
                        "PRIMARY KEY (id)" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " +
                    "TodoEntries(" +
                        "id INTEGER," +
                        "listId INTEGER," +
                        "content TEXT NOT NULL," +
                        "checked BOOLEAN," +
                        "PRIMARY KEY (id)," +
                        "FOREIGN KEY (listId) REFERENCES TodoLists(id) ON DELETE CASCADE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " +
                    "TodoUsers(" +
                        "listId INT," +
                        "userId VARCHAR(18) NOT NULL," +
                        "PRIMARY KEY (listId, userId)," +
                        "FOREIGN KEY (listId) REFERENCES TodoLists(id) ON DELETE CASCADE" +
                    ")");

            //Permissions
            preparedStatements.put("addOp", conn.prepareStatement("REPLACE INTO Ops (id) VALUES (?)"));
            preparedStatements.put("getOps", conn.prepareStatement("SELECT id FROM Ops"));
            preparedStatements.put("removeOp", conn.prepareStatement("DELETE FROM Ops WHERE id = ?"));

            //TodoCommand
            preparedStatements.put(TodoCommand.ADD_TODO_LIST, conn.prepareStatement("INSERT INTO TodoLists (label, owner, locked) VALUES (?, ?, ?)"));
            preparedStatements.put(TodoCommand.ADD_TODO_ENTRY, conn.prepareStatement("INSERT INTO TodoEntries (listId, content, checked) VALUES (?, ?, ?)"));
            preparedStatements.put(TodoCommand.ADD_TODO_USER, conn.prepareStatement("INSERT INTO TodoUsers (listId, userId) VALUES (?, ?)"));
            preparedStatements.put(TodoCommand.GET_TODO_LISTS, conn.prepareStatement("SELECT id, label, owner, locked FROM TodoLists"));
            preparedStatements.put(TodoCommand.GET_TODO_ENTRIES, conn.prepareStatement("SELECT id, content, checked FROM TodoEntries WHERE listId = ?"));
            preparedStatements.put(TodoCommand.GET_TODO_USERS, conn.prepareStatement("SELECT userId FROM TodoUsers WHERE listId = ?"));
            preparedStatements.put(TodoCommand.SET_TODO_LIST_LOCKED, conn.prepareStatement("UPDATE TodoListS SET locked = ? WHERE id = ?"));
            preparedStatements.put(TodoCommand.SET_TODO_ENTRY_CHECKED, conn.prepareStatement("UPDATE TodoEntries SET checked = ? WHERE id = ?"));
            preparedStatements.put(TodoCommand.SET_TODO_ENTRIES_CHECKED, conn.prepareStatement("UPDATE TodoEntries SET checked = ? WHERE listId = ?"));
            preparedStatements.put(TodoCommand.REMOVE_TODO_LIST, conn.prepareStatement("DELETE FROM TodoLists WHERE id = ?"));
            preparedStatements.put(TodoCommand.REMOVE_TODO_ENTRY, conn.prepareStatement("DELETE FROM TodoEntries WHERE id = ?"));
            preparedStatements.put(TodoCommand.REMOVE_TODO_USER, conn.prepareStatement("DELETE FROM TodoUsers WHERE listId = ? AND userId = ?"));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public PreparedStatement getStatement(String statementName)
    {
        if (!preparedStatements.containsKey(statementName))
            throw new RuntimeException("The statement: '" + statementName + "' does not exist.");
        return preparedStatements.get(statementName);
    }

    public static int getAutoIncrement(PreparedStatement executedStatement, int col) throws SQLException
    {
        ResultSet autoIncrements = executedStatement.getGeneratedKeys();
        autoIncrements.next();
        return autoIncrements.getInt(col);
    }
}
