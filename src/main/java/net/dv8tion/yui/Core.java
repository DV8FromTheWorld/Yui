/*
 *      Copyright 2015-2016 Austin Keener
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

package net.dv8tion.yui;

import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.utils.SimpleLog;
import net.dv8tion.yui.config.Configuration;
import net.dv8tion.yui.config.Property;
import net.dv8tion.yui.plugin.Plugin;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONException;
import org.json.JSONObject;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.xeustechnologies.jcl.JclUtils;
import sun.misc.IOUtils;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class Core
{
    //Non error, no action exit codes.
    public static final int NORMAL_SHUTDOWN = 10;
    public static final int RESTART_EXITCODE = 11;
    public static final int NEWLY_CREATED_CONFIG = 12;

    //Non error, action required exit codes.
    public static final int UPDATE_LATEST_EXITCODE = 20;
    public static final int UPDATE_RECOMMENDED_EXITCODE = 21;

    //error exit codes.
    public static final int UNABLE_TO_CONNECT_TO_DISCORD = 30;
    public static final int BAD_USERNAME_PASS_COMBO = 31;
    public static final int NO_USERNAME_PASS_COMBO = 32;

    public static final File CONFIG_FILE = new File("Yui.cfg");
    public static final File PLUGINS_DIR = new File("./plugins");
    public static final SimpleLog LOG = SimpleLog.getLog("Yui");

    public static final HashMap<String, Triple<Plugin, JarClassLoader, File>> plugins = new HashMap<>();

    private static String botToken;

    private static boolean proxyEnable;
    private static String proxyUrl;
    private static int proxyPort;

    public static void main(String[] args) throws MalformedURLException, InterruptedException
    {
        if (!CONFIG_FILE.exists())
        {
            LOG.info("Based on a lack of Yui.cfg, assuming first time run. Generating default configs");
            loadConfig();
            searchForPlugins();
            //loadPluginConfigs();
            LOG.info("Default configs for Yui and all discovered plugins have been generated. Before restarting, modify configs as needed.\n" +
                    "Specifically, be sure to provide a bot token in Yui.cfg");
            System.exit(NEWLY_CREATED_CONFIG);
        }
        loadConfig();
        searchForPlugins();
        //TODO: Get info from cmd line args.

        JDABuilder builder = new JDABuilder().setBotToken(botToken);
        if (proxyEnable)
            builder.setProxy(proxyUrl, proxyPort);

        try
        {
            builder.buildAsync();
        }
        catch (IllegalArgumentException e)
        {
            LOG.fatal("No login details provided! Please provide a botToken in the config.");
            System.exit(NO_USERNAME_PASS_COMBO);
        }
        catch (LoginException e)
        {
            LOG.fatal("The botToken provided in Yui.cfg was incorrect.");
            LOG.fatal("Did you modify Yui.cfg after it was created?");
            System.exit(BAD_USERNAME_PASS_COMBO);
        }
    }

    public static void loadConfig()
    {
        Configuration cfg = new Configuration(CONFIG_FILE);
        Property prop;

        prop = cfg.get("general", "discordToken", "");
        prop.setComment("The token used to login to discord");
        botToken = prop.getString();

        prop = cfg.get("general", "logLevel-Yui", "info");
        prop.setComment("Used to change the output logger level of Yui's Systems. Options: [fatal, warn, info, debug, trace, off]");
        String sLevel = prop.getString();
        SimpleLog.Level level = getLevel(prop.getString());
        if (level == null)
        {
            LOG.warn("The Log-Level in Yui.cfg was set to an unknown level type: `" + sLevel + "`. Defaulting to Level.INFO");
            level = SimpleLog.Level.INFO;
        }
        LOG.setLevel(level);

        prop = cfg.get("general", "logLevel-JDA", "info");
        prop.setComment("Used to change the output logger level of JDA's Systems. Options: [fatal, warn, info, debug, trace, off]");
        sLevel = prop.getString();
        level = getLevel(prop.getString());
        if (level == null)
        {
            LOG.warn("The JDA-Log-Level in Yui.cfg was set to an unknown level type: `" + sLevel + "`. Defaulting to Level.INFO");
            level = SimpleLog.Level.INFO;
        }
        SimpleLog.LEVEL = level;

        prop = cfg.get("proxy", "proxyEnabled", false);
        prop.setComment("Whether or not to actually use the proxy settings");
        proxyEnable = prop.getBoolean();

        prop = cfg.get("proxy", "proxyHost", "");
        prop.setComment("The proxy host url");

        prop = cfg.get("proxy", "proxyPort", 8080);
        prop.setComment("The proxy port");

        if (cfg.hasChanged())
            cfg.save();
    }

    public static void searchForPlugins()
    {
        LOG.info("Searching for plugins...");
        if (!PLUGINS_DIR.exists())
        {
            if (PLUGINS_DIR.mkdirs())
                LOG.info("Plugins directory not found. Generated new directory.");
            else    //TODO: probably should kill Yui here.
                LOG.fatal("Plugins directory not found and Yui could not generate directory. Possible issue with file/folder write permissions?");
            return;
        }

        File[] files = PLUGINS_DIR.listFiles();
        LOG.debug("Found " + files.length + " files/folders in the plugins dir.");
        for (File f : files)
        {
            if (f.isDirectory() || !f.getName().endsWith(".jar"))
                continue;

            try
            {
                JarFile jar = new JarFile(f);
                ZipEntry infoFile = jar.getEntry("plugin.info");
                if (infoFile == null)
                {
                    LOG.trace("Found jar when loading plugins that doesn't contain a plugin.info.\n" +
                            "File: " + f.getName());
                    continue;
                }

                byte[] infoData = IOUtils.readFully(jar.getInputStream(infoFile), -1, false);
                if (infoData == null || infoData.length == 0)
                {
                    LOG.warn("Found jar when loading plugins that contains an plugin.info file, but it is empty. Skipping plugin.\n" +
                            "File: " + f.getName());
                    continue;
                }

                try
                {
                    JSONObject infoJson = new JSONObject(new String(infoData));
                    String pluginPath = infoJson.optString("plugin", null);
                    if (pluginPath == null || pluginPath.isEmpty())
                    {
                        LOG.warn("Provided plugin.info in plugin does not specify a classpath to the Plugin core.\n" +
                                "Example: \"plugin\":\"net.plugin.MyPluginClass\"\n" +
                                "File: " + f.getName());
                        continue;
                    }

                    JclObjectFactory factory = JclObjectFactory.getInstance();
                    JarClassLoader cl = new JarClassLoader();
                    cl.add(f.toURI().toURL());

                    Object oPlugin = factory.create(cl, pluginPath);
                    Plugin plugin = JclUtils.cast(oPlugin, Plugin.class);

                    String id = plugin.getIdentifier();
                    if (id == null || id.isEmpty())
                        id = f.getAbsolutePath();

                    if (plugins.containsKey(id))
                    {
                        LOG.warn("Found plugin with an id that has already been loaded! Skipping conflicting plugin.\n" +
                                "id: " + id + "\n" +
                                "Loaded Plugin: " + plugins.get(id).getRight().getName() + "\n" +
                                "Conflicting Plugin: " + f.getName());
                        continue;
                    }

                    LOG.debug("Discovered plugin: " + plugin.getName());
                    plugins.put(id, Triple.<Plugin, JarClassLoader, File>of(plugin, cl, f));
                }
                catch (JSONException e)
                {
                    LOG.warn("Provided plugin.info is incorrect json format.\n" +
                            "Follow error occured: " + e.getMessage() + "\n" +
                            "File: " + f.getName());
                    continue;
                }
            }
            catch (IOException e)
            {
                //It isn't a JarFile, so we're done here.
            }
        }
        LOG.info("Finished searching for plugins. Plugins Found: " + plugins.size());
    }

    public static SimpleLog.Level getLevel(String key)
    {
        if (key == null)
            return null;
        key = key.toLowerCase();
        switch (key)
        {
            case "fatal":
                return SimpleLog.Level.FATAL;
            case "warn":
                return SimpleLog.Level.WARNING;
            case "info":
                return SimpleLog.Level.INFO;
            case "debug":
                return SimpleLog.Level.DEBUG;
            case "trace":
                return SimpleLog.Level.TRACE;
            case "off":
                return SimpleLog.Level.OFF;
            default:
                return null;
        }
    }
}
