package net.dv8tion.discord.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;
import net.dv8tion.discord.Permissions;

public class PermissionsCommand extends Command
{

    @Override
    public void onChat(UserChatEvent e)
    {
        if (!containsCommand(e.getMsg()))
            return;

//        if (!Permissions.getPermissions().isOp(e.getUser()))
//        {
//            e.getGroup().sendMessage(new MessageBuilder()
//                .addUserTag(e.getUser(), e.getGroup())
//                .addString(": " + "You do not have permission to run this command! (OP required).")
//                .build());
//            return;
//        }

        String[] args = commandArgs(e.getMsg());
        switch (args[0])
        {
            case ".perms":
            case ".permissions":
                if (args[1].equals("op"))
                {
                    if (args.length == 4)
                    {
                        if (args[2].equals("add"))
                        {
                            Pattern idPattern = Pattern.compile("(?<=<@)[0-9]{18}(?=>)");
                            Matcher idMatch = idPattern.matcher(args[3]);
                            if (!idMatch.find())
                            {
                                e.getGroup().sendMessage(new MessageBuilder()
                                    .addUserTag(e.getUser(), e.getGroup())
                                    .addString(": " + "Sorry, I don't recognize the user provided: " + args[3])
                                    .build());
                                return;
                            }
                            try
                            {
                                if (Permissions.getPermissions().addOp(idMatch.group()))
                                {
                                    e.getGroup().sendMessage(new MessageBuilder()
                                        .addUserTag(e.getUser(), e.getGroup())
                                        .addString(": " + "Successfully added ")
                                        .addUserTag(e.getServer().getGroupUserById(idMatch.group()), e.getGroup())
                                        .addString(" to the OPs list!")
                                        .build());
                                    return;
                                }
                                else
                                {
                                    e.getGroup().sendMessage(new MessageBuilder()
                                        .addUserTag(e.getUser(), e.getGroup())
                                        .addString(": ")
                                        .addUserTag(e.getServer().getGroupUserById(idMatch.group()), e.getGroup())
                                        .addString(" is already an OP!")
                                        .build());
                                    return;
                                }
                            }
                            catch (Exception e1)
                            {
                                e1.printStackTrace();
                            }
                        }
                        else if (args[2].equals("remove"))
                        {

                        }
                        else
                        {
                            e.getGroup().sendMessage(new MessageBuilder()
                                .addUserTag(e.getUser(), e.getGroup())
                                .addString(": " + "**Improper syntax, unrecognized argument:** " + args[2])
                                .addString("\n**Provided Command:** " + e.getMsg().toString())
                                .build());
                            return;
                        }
                    }
                    else if (args.length == 3 && args[2].equals("list"))
                    {
                        String ops = "";
                        for (String op : Permissions.getPermissions().getOps())
                        {
                            ops += "<@" + op + "> ";
                        }
                        e.getGroup().sendMessage(new MessageBuilder()
                            .addUserTag(e.getUser(), e.getGroup())
                            .addString(": My OPs are: [" + ops.trim() + "]")
                            .build());
                        return;
                    }
                }
                else
                {
                    e.getGroup().sendMessage(new MessageBuilder()
                        .addUserTag(e.getUser(), e.getGroup())
                        .addString(": " + "**Improper syntax, unrecognized argument:** " + args[1])
                        .addString("\n**Provided Command:** " + e.getMsg().toString())
                        .build());
                    return;
                }
                break;
            case ".op":
                if (args.length < 3)
                {
                    e.getGroup().sendMessage(new MessageBuilder()
                        .addUserTag(e.getUser(), e.getGroup())
                        .addString(": " + "Improper syntax, requires 2 arguments")
                        .build());
                    return;
                }
                break;
        }
        //CommandSyntax:  .perms op add @<name>  .perms op remove @<name>  .perms op list
        //Or:   .op add @<name>   .op remove @<name>
    }

    @Override
    public List<String> aliases()
    {
        return Arrays.asList(new String[] {".perms", ".permissions", ".op"});
    }

    @Override
    public String commandDescription()
    {
        return "Used to modify the permissions of the provided user.";
    }

    @Override
    public String helpMessage()
    {
        return null;
    }
}
