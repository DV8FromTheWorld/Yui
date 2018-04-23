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
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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
            engine.eval("var imports = new JavaImporter(" +
                    "java.io," +
                    "java.lang," +
                    "java.util," +
                    "Packages.net.dv8tion.jda.core," +
                    "Packages.net.dv8tion.jda.core.entities," +
                    "Packages.net.dv8tion.jda.core.entities.impl," +
                    "Packages.net.dv8tion.jda.core.managers," +
                    "Packages.net.dv8tion.jda.core.managers.impl," +
                    "Packages.net.dv8tion.jda.core.utils);");
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
            engine.put("message", e.getMessage());
            engine.put("channel", e.getChannel());
            engine.put("args", args);
            engine.put("api", e.getJDA());
            if (e.isFromType(ChannelType.TEXT))
            {
                engine.put("guild", e.getGuild());
                engine.put("member", e.getMember());
            }

            Object out = engine.eval(
                    "(function() {" +
                        "with (imports) {" +
                            e.getMessage().getContentDisplay().substring(args[0].length()) +
                        "}" +
                    "})();");
            sendMessage(e, out == null ? "Executed without error." : out.toString());
        }
        catch (Exception e1)
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
