package net.dv8tion.yui.plugins;

import net.dv8tion.yui.plugin.Plugin;
import net.dv8tion.yui.plugin.PluginBundle;
import net.dv8tion.yui.plugin.Version;

public class Test implements Plugin
{
    @Override
    public String getName()
    {
        return "Test Plugin";
    }

    @Override
    public String getIdentifier()
    {
        return getName();
    }

    @Override
    public Version getPluginVersion()
    {
        return new Version("0.0.0_0");
    }

    @Override
    public Version getApiVersion()
    {
        return new Version("0.0.1_0");
    }

    @Override
    public void load(PluginBundle bundle)
    {
        System.out.println("Got a load!");
    }

    @Override
    public void unload(PluginBundle bundle)
    {
        System.out.println("Was asked to unload. aww..");
    }
}
