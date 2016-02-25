package net.dv8tion.discord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.dv8tion.discord.util.Database;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.User;

public class Permissions
{
    public static final String ADD_OP = "addOp";
    public static final String GET_OPS = "getOps";
    public static final String REMOVE_OPS = "removeOp";

    public static final String OP_REQUIRED_MESSAGE = "You do not have permission to run this command! (OP required).";

    private static Permissions permissions;
    private ArrayList<String> ops;

    private Permissions()
    {
        ops = new ArrayList<String>();
        try
        {
            ResultSet opsSet = Database.getInstance().getStatement(GET_OPS).executeQuery();
            while (opsSet.next())
            {
                ops.add(opsSet.getString("id"));
            }
            if (ops.size() == 0)
            {
                //TODO: Once we interact with the CLI, request an OP here. For now, default to make bot OP.
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void setupPermissions()
    {
        if (permissions != null)
            throw new RuntimeException("We are trying to setup permissions again!?");
        permissions = new Permissions();
    }

    public static Permissions getPermissions()
    {
        return permissions;
    }

    /**
     * Used to set the bot as an OP after the jDiscord API has connected and acquired
     * the login account's ID.
     *
     * @param bot
     *          The User object representing the currently logged in account.
     */
    public void setBotAsOp(SelfInfo bot)
    {
        ops.add(bot.getId());
    }

    /**
     * Adds the specified user to the Ops list and DB table.
     *
     * @param userId
     *          The Discord ID of the user.
     * @return
     *      true - if the user was successfully added.
     *      false - if the user was already an OP.
     * @throws SQLException
     */
    public Boolean addOp(String userId) throws SQLException
    {
        if (ops.contains(userId))
            return false;

        PreparedStatement addOp = Database.getInstance().getStatement(ADD_OP);
        addOp.setString(1, userId);
        if (addOp.executeUpdate() == 1)
        {
            return ops.add(userId);
        }
        return false;
    }

    /**
     * Removes the specified user from the Ops list and DB table.
     *
     * @param userId
     *          The Discord ID of the user.
     * @return
     *      true - if the user was successfully removed.
     *      false - if the user was not an op.
     */
    public boolean removeOp(String userId) throws SQLException
    {
        if (!ops.contains(userId))
            return false;

        PreparedStatement removeOp = Database.getInstance().getStatement(REMOVE_OPS);
        removeOp.setString(1, userId);
        if (removeOp.executeUpdate() == 1)
        {
            return ops.remove(userId);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> getOps()
    {
        return (ArrayList<String>) ops.clone();
    }

    /**
     * Used to check the permissions of a user.
     *
     * @param user
     *          The GroupUser to check the permission level of.
     * @return
     *      true - if the user is considered an OP.
     */
    public boolean isOp(User user)
    {
        return isOp(user.getId());
    }

    /**
     * Used to check the permissions of a user.
     *
     * @param userId
     *          The Discord ID of the user.
     * @return
     *      true - if the user is considered an OP.
     */
    public boolean isOp(String userId)
    {
        return ops.contains(userId);
    }
}
