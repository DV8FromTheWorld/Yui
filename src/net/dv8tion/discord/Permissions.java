package net.dv8tion.discord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Permissions
{
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
                ops.add("111761808640978944");  //This is Yui's Discord User ID. Will get it programmibly later.
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
     * Adds the specified user to the Ops list and DB table.
     *
     * @param userId
     *          The Discord ID of the user.
     * @return
     *      true - if the user was successfully added.
     *      false - if the user was already an OP.
     *      null - if there was an exception.
     */
    public Boolean addOp(String userId)
    {
        if (ops.contains(userId))
            return false;
        ops.add(userId);

        PreparedStatement addOp;
        try
        {
            addOp = Database.getInstance().getStatement("addOp");
            addOp.setString(1, userId);
            return addOp.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Removes the specified user from the Ops list and DB table.
     *
     * @param userId
     *          The Discord ID of the user.
     * @return
     *      true - if the user was successfully removed.
     *      false - if the user was not an op.
     *      null - if there was an exception.
     */
    public Boolean removeOp(String userId)
    {
        if (!ops.contains(userId))
            return false;
        ops.remove(userId);

        PreparedStatement removeOp;
        try
        {
            removeOp = Database.getInstance().getStatement("removeOp");
            removeOp.setString(1, userId);
            return removeOp.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
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
