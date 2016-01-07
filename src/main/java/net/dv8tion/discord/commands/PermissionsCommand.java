package net.dv8tion.discord.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.discord.Bot;
import net.dv8tion.discord.Permissions;

import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.ArrayUtils;

public class PermissionsCommand extends Command
{

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        if (!Permissions.getPermissions().isOp(e.getAuthor()))
        {
            sendMessage(e, Permissions.OP_REQUIRED_MESSAGE);
            return;
        }

        if (args[0].contains(".perms") || args[0].contains(".permissions"))
        {
            args = ArrayUtils.subarray(args, 1, args.length);   //We cut off the .perms or .permissions to make the array behave as .op would
        }
        else
        {
            args[0] = args[0].replace(".", "");     //Cut off the leading .
        }

        if (args.length < 1)    //If the command sent was just '.perms', and we removed that above, then we have an array of length 0 currently.
        {
            sendMessage(e, "**Improper syntax, no permissions group provided!**");
            return;
        }
        switch (args[0])
        {
            //Only 1 case for now. Later we will have more user permissions types...probably.
            case "op":
                processOp(e, args);
                break;
            default:
                sendMessage(e, new StringBuilder()
                        .append("**Improper syntax, unrecognized permission group:** " + args[0])
                        .append("\n**Provided Command:** " + e.getMessage().getContent())
                        .toString());
                return;
        }
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".perms", ".permissions", ".op");
    }

    @Override
    public String getDescription()
    {
        return "Used to modify the permissions of the provided user.";
    }

    @Override
    public String getName()
    {
        return "Permissions Management";
    }

    @Override
    public String getUsageInstructions()
    {
        return ".perms *<group> <action> <user>*\n"
                + "Groups:  [op]\n"
                + "Actions: [add, remove, list]\n"
                + "User:  Must be an @Mentioned user.\n"
                + "__Example:__   .perms op add @DV8FromTheWorld\n"
                + " - This would add the user 'DV8FromTheWorld' to the OPs list.\n"
                + "__Example 2:__ .perms op list\n"
                + " - This would list all bot OPs.\n"
                + "\n"
                + "**NOTE:** you can skip the .perms and jump straight to the group by using the group alias.\n"
                + "__Example:__  .op remove @BananaPhone";
    }

    /**
     * This processes all commands of the format:  .perms op / .permission op / .op
     *
     * @param args
     *          The array of arguments that represent the .perms/.permissions removed command.
     * @param e
     *          The original UserChatEvent, used to sendMessages.
     */
    private void processOp(MessageReceivedEvent e, String[] args)
    {
        if (args.length < 2)
        {
            sendMessage(e, "**Improper syntax, no action argument provided!**");
            return;
        }
        switch (args[1])
        {
            case "add":
                processAddOp(e, args);
                break;
            case "remove":
                processRemoveOp(e, args);
                break;
            case "list":
                String ops = "";
                for (String op : Permissions.getPermissions().getOps())
                {
                    ops += "<@" + op + "> ";
                }
                sendMessage(e, "My OPs are: [" + ops.trim() + "]");
                break;
            default:
                sendMessage(e, new StringBuilder()
                    .append("**Improper syntax, unrecognized argument:** " + args[1])
                    .append("\n**Provided Command:** " + e.getMessage().getContent())
                    .toString());
        }
    }

    /**
     * This processes the addOp commands of the format:  .perms op add/ .permission op add/ .op add
     *
     * @param args
     *          The array of arguments that represent the .perms/.permissions removed command.
     * @param e
     *          The original UserChatEvent, used to sendMessages.
     */
    private void processAddOp(MessageReceivedEvent e, String[] args)
    {
        if (args.length < 3)
        {
            sendMessage(e, "Please provide a user!");
            return;
        }
        Pattern idPattern = Pattern.compile("(?<=<@)[0-9]{18}(?=>)");
        Matcher idMatch = idPattern.matcher(args[2]);
        if (!idMatch.find())
        {
            sendMessage(e, "Sorry, I don't recognize the user provided: " + args[2]);
            return;
        }
        try
        {
            User user = Bot.getAPI().getUserById(idMatch.group());
            String username = user != null ? user.getUsername() : "<@" + idMatch.group() + ">";

            if (Permissions.getPermissions().addOp(idMatch.group()))
            {
                sendMessage(e, "Successfully added " + username + " to the OPs list!");
                return;
            }
            else
            {
                sendMessage(e, username + " is already an OP!");
                return;
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    /**
     * This processes the removeOp commands of the format:  .perms op remove/ .permission op remove/ .op remove
     *
     * @param args
     *          The array of arguments that represent the .perms/.permissions removed command.
     * @param e
     *          The original UserChatEvent, used to sendMessages.
     */
    private void processRemoveOp(MessageReceivedEvent e, String[] args)
    {
        if (args.length < 3)
        {
            sendMessage(e, "Please provide a user!");
            return;
        }
        Pattern idPattern = Pattern.compile("(?<=<@)[0-9]{18}(?=>)");
        Matcher idMatch = idPattern.matcher(args[2]);
        if (!idMatch.find())
        {
            sendMessage(e, "Sorry, I don't recognize the user provided: " + args[2]);
            return;
        }
        try
        {
            User user = Bot.getAPI().getUserById(idMatch.group());
            String username = user != null ? user.getUsername() : "<@" + idMatch.group() + ">";

            if (Permissions.getPermissions().removeOp(idMatch.group()))
            {
                sendMessage(e, "Successfully removed " + username + " from the OPs list!");
                return;
            }
            else
            {
                sendMessage(e, username + " cannot be removed because they weren't an OP!");
                return;
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }
}
