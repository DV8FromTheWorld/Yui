package net.dv8tion.discord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.dv8tion.discord.util.Database;
import me.itsghost.jdiscord.SelfData;
import me.itsghost.jdiscord.talkable.GroupUser;

public class Permissions
{
    public static final String OP_REQUIRED_MESSAGE = "You do not have permission to run this command! (OP required).";

    private static Permissions permissions;
    private ArrayList<String> ops;

    private Permissions()
    {
        ops = new ArrayList<String>();
        try
        {
            ResultSet opsSet = Database.getInstance().getStatement("getOps").executeQuery();
            while (opsSet.next())
            {
                ops.add(opsSet.getString("id"));
            }
            if (ops.size() == 0)
            {
                //TODO: Once we interact with the CLI, request an OP here. For now, default to make bot OP.
//                ops.add(Bot.getAPI().getSelfInfo().getId());
                ops.add("111761808640978944");  //This is Yui's Discord User ID. Will get it programmably later.
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
    public void setBotAsOp(SelfData bot)
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
    public Boolean addOp(String userId) throws SQLException, Exception
    {
        if (ops.contains(userId))
            return false;

        PreparedStatement addOp = Database.getInstance().getStatement("addOp");
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
     * @throws SQLException
     */
    public boolean removeOp(String userId) throws SQLException, Exception
    {
        if (!ops.contains(userId))
            return false;

        PreparedStatement removeOp = Database.getInstance().getStatement("removeOp");
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
    public boolean isOp(GroupUser user)
    {
        return isOp(user.getUser().getId());
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
