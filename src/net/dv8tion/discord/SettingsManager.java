/**
 * This code came directly from Smbarbour's RavenBot.
 * https://github.com/MCUpdater/RavenBot/blob/master/src/main/java/org/mcupdater/ravenbot/SettingsManager.java
 */
package net.dv8tion.discord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SettingsManager {
    private static SettingsManager instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Settings settings;
    private final Path configFile = new File(".").toPath().resolve("Config.json");

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public SettingsManager() {
        if (!configFile.toFile().exists()) {
            System.out.println("SettingsManager: Creating default settings");
            System.out.println("SettingsManager: You will need to edit the Config.json with your login information.");
            this.settings = getDefaultSettings();
            saveSettings();
            return;
        }
        loadSettings();
    }

    public void loadSettings() {
        try {
            checkBadEscapes(configFile);

            BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8);
            this.settings = gson.fromJson(reader, Settings.class);
            reader.close();
            System.out.println("SettingsManager: Settings loaded");
            checkOldSettingsFile();
        } catch (IOException e) {
            System.out.println("SettingsManager: Error Loading Settings");
            e.printStackTrace();
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void saveSettings() {
        String jsonOut = gson.toJson(this.settings);
        try {
            BufferedWriter writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8);
            writer.append(jsonOut);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Settings getDefaultSettings() {
        Settings newSettings = new Settings();
        newSettings.setEmail("email");
        newSettings.setPassword("password");
        newSettings.setGithubRepoUrl("https://github.com/DV8FromTheWorld/Discord-Bot");
        newSettings.setJavaJDKPath("");
        return newSettings;
    }

    private void checkOldSettingsFile()
    {
        Settings defaults = getDefaultSettings();
        if (settings.getEmail() == null) settings.setEmail(defaults.getEmail());
        if (settings.getPassword() == null) settings.setPassword(defaults.getPassword());
        if (settings.getGithubRepoUrl() == null) settings.setGithubRepoUrl(defaults.getGithubRepoUrl());
        if (settings.getJavaJDKPath() == null) settings.setJavaJDKPath(defaults.getJavaJDKPath());
        saveSettings();
    }

    private void checkBadEscapes(Path filePath) throws IOException
    {
        boolean modified = false;
        byte[] bytes = Files.readAllBytes(filePath);
        ArrayList<Byte> checkedBytes = new ArrayList<Byte>();

        boolean expectingBackwardsSolidus = false;
        for (byte b : bytes)
        {
            if (b == 92) //If it is a \
            {
                if (expectingBackwardsSolidus) //If there was already a \, then we don't need to find another
                {
                    expectingBackwardsSolidus = false;
                }
                else //If there wasn't a preceding \, then we need another one.
                {
                    expectingBackwardsSolidus = true;
                }
            }
            else if (expectingBackwardsSolidus) //If is isn't a \, but we were expecting one
            {
                modified = true;
                expectingBackwardsSolidus = false;
                checkedBytes.add((byte) 92);
            }
            checkedBytes.add(b);
        }

        if (modified)
        {
            Byte[] output = checkedBytes.toArray(new Byte[checkedBytes.size()]);
            Files.write(filePath, ArrayUtils.toPrimitive(output));
        }
    }
}
