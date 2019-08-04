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
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                sendMessage(e, new MessageBuilder()
                        .append("**Improper syntax, unrecognized permission group:** ")
                        .append(args[0])
                        .append("\n**Provided Command:** ")
                        .append(e.getMessage().getContentDisplay())
                        .build());
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
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
                ".perms *<group> <action> <user>*\n"
                + "Groups:  [op]\n"
                + "Actions: [add, remove, list]\n"
                + "User:  Must be an @Mentioned user.\n"
                + "__Example:__   .perms op add @DV8FromTheWorld\n"
                + " - This would add the user 'DV8FromTheWorld' to the OPs list.\n"
                + "__Example 2:__ .perms op list\n"
                + " - This would list all bot OPs.\n"
                + "\n"
                + "**NOTE:** you can skip the .perms and jump straight to the group by using the group alias.\n"
                + "__Example:__  .op remove @BananaPhone");
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
                boolean notFirstLoop = false;
                StringBuilder builder = new StringBuilder();
                builder.append("My OPs are:  [");
                for (String op : Permissions.getPermissions().getOps())
                {
                    if (notFirstLoop)
                        builder.append(", ");
                    User user = e.getJDA().getUserById(op);
                    if (user != null)
                        builder.append(user.getName());
                    else
                        builder.append("<@").append(op).append(">");
                    notFirstLoop = true;
                }
                builder.append("]");
                sendMessage(e, builder.toString());
                break;
            default:
                sendMessage(e, new MessageBuilder()
                    .append("**Improper syntax, unrecognized argument:** ")
                    .append(args[1])
                    .append("\n**Provided Command:** ")
                    .append(e.getMessage().getContentDisplay())
                    .build());
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
        if (args.length < 3 || e.getMessage().getMentionedUsers().isEmpty())
        {
            sendMessage(e, "Please provide a user!");
            return;
        }

        for (User user : e.getMessage().getMentionedUsers())
    {
        try
        {
            if (Permissions.getPermissions().addOp(user.getId()))
            {
                sendMessage(e, "Successfully added " + user.getName() + " to the OPs list!");
                return;
            }
            else
            {
                sendMessage(e, user.getName() + " is already an OP!");
                return;
            }
        }
        catch (Exception e1)
        {
            sendMessage(e, new MessageBuilder()
                    .append("Encountered an error when attempting to add OP.\n")
                    .append("User: ").append(user.getName())
                    .append("Error: ").append(e1.getClass().getName()).append("\n")
                    .append("Reason: ").append(e1.getMessage())
                    .build());
        }
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
        if (args.length < 3 || e.getMessage().getMentionedUsers().isEmpty())
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

        for (User user : e.getMessage().getMentionedUsers())
        {
            try
            {
                if (Permissions.getPermissions().removeOp(user.getId()))
                {
                    sendMessage(e, "Successfully removed " + user.getName() + " to the OPs list!");
                    return;
                }
                else
                {
                    sendMessage(e, user.getName() + " cannot be removed because they weren't an OP!");
                    return;
                }
            }
            catch (Exception e1)
            {
                sendMessage(e, new MessageBuilder()
                        .append("Encountered an error when attempting to remove OP.\n")
                        .append("User: ").append(user.getName())
                        .append("Error: ").append(e1.getClass().getName()).append("\n")
                        .append("Reason: ").append(e1.getMessage())
                        .build());
            }
        }
    }
}
