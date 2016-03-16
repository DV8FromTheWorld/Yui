package net.dv8tion.discord.commands;

import net.dv8tion.discord.Permissions;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.List;

public class EvalCommand extends Command
{
    private ScriptEngine engine;

    public EvalCommand()
    {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        try
        {
            engine.eval("var imports = new JavaImporter(java.io, java.lang, java.util);");
        }
        catch (ScriptException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        //Specifically ignores the user meew0 due to a conflict between his bot (Elgyem) and Yui.
        //We both agreed to make our bots ignore eachother's .eval commands.
        if (e.getAuthor().getId().equals("66237334693085184"))  //meew0's ID
            return;

        if (!Permissions.getPermissions().isOp(e.getAuthor()))
        {
            sendMessage(e, "Sorry, this command is OP only!");
            return;
        }

        try
        {
            engine.put("event", e);
            engine.put("channel", e.getChannel());
            engine.put("args", args);
            engine.put("api", e.getJDA());
            Object out = engine.eval(
                    "(function() {" +
                        "with (imports) {" +
                            e.getMessage().getContent().substring(args[0].length()) +
                        "}" +
                    "})();");
            sendMessage(e, out == null ? "Executed without error." : out.toString());
        }
        catch (ScriptException e1)
        {
            sendMessage(e, e1.getMessage());
        }
    }

    @Override
    public List<String> getAliases()
    {
        return Collections.singletonList(".eval");
    }

    @Override
    public String getDescription()
    {
        return "Takes Java or Javascript and executes it.";
    }

    @Override
    public String getName()
    {
        return "Evaluate";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Collections.singletonList(
                ".eval <Java code>\n" +
                "    Example: `.eval return \"5 + 5 is: \" + (5 + 5);\n" +
                "    This will print: 5 + 5 is: 10");
    }
}
