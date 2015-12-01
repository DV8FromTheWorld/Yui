package net.dv8tion.discord.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Ops(id)");
            preparedStatements.put("addOp", conn.prepareStatement("REPLACE INTO Ops (id) VALUES (?)"));
            preparedStatements.put("removeOp", conn.prepareStatement("DELETE FROM Ops WHERE id = ?"));
            preparedStatements.put("getOps", conn.prepareStatement("SELECT id FROM Ops"));
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

    public PreparedStatement getStatement(String statementName) throws Exception
    {
        if (!preparedStatements.containsKey(statementName))
            throw new Exception("The statement: '" + statementName + "' does not exist.");
        return preparedStatements.get(statementName);
    }
}
