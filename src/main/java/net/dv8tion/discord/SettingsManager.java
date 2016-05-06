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
package net.dv8tion.discord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import net.dv8tion.discord.bridge.BridgeInfo;
import net.dv8tion.discord.bridge.IrcConnectInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This code came directly from Smbarbour's RavenBot.
 * https://github.com/MCUpdater/RavenBot/blob/master/src/main/java/org/mcupdater/ravenbot/SettingsManager.java
 */
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
            System.exit(Yui.NEWLY_CREATED_CONFIG);
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
        newSettings.setBotToken("");
        newSettings.setGoogleApiKey("");
        newSettings.setProxyHost("");
        newSettings.setProxyPort("8080");
        newSettings.setUseBetaBuilds(new Boolean(false));

        IrcConnectInfo connectDefault = new IrcConnectInfo();
        connectDefault.setIdentifier("IrcConnection1");
        connectDefault.setHost("");
        connectDefault.setPort(6667);
        connectDefault.setNick("");
        connectDefault.setIdentNick("");
        connectDefault.setIdentPass("");
        connectDefault.setAutojoinChannels(Arrays.asList(""));
        newSettings.setIrcConnectInfos(Arrays.asList(connectDefault));

        ArrayList<BridgeInfo> bridgesDefault = new ArrayList<BridgeInfo>();
        newSettings.setBridges(bridgesDefault);
        return newSettings;
    }

    private void checkOldSettingsFile()
    {
        boolean modified = false;
        Settings defaults = getDefaultSettings();
        if (settings.getBotToken() == null)
        {
            settings.setBotToken(defaults.getBotToken());
            modified = true;
        }
        if (settings.getGoogleApiKey() == null)
        {
            settings.setGoogleApiKey(defaults.getGoogleApiKey());
            modified = true;
        }
        if (settings.getUseBetaBuilds() == null)
        {
            settings.setUseBetaBuilds(defaults.getUseBetaBuilds());
            modified = true;
        }
        if (settings.getIrcConnectInfos() == null)
        {
            settings.setIrcConnectInfos(defaults.getIrcConnectInfos());
            modified = true;
        }
        if (settings.getBridges() == null)
        {
            settings.setBridges(defaults.getBridges());
            modified = true;
        }
        if (settings.getProxyHost() == null)
        {
            settings.setProxyHost(defaults.getProxyHost());
            modified = true;
        }
        if (settings.getProxyPort() == null)
        {
            settings.setProxyPort(defaults.getProxyPort());
            modified = true;
        }

        if (modified)
            saveSettings();
    }

    private void checkBadEscapes(Path filePath) throws IOException
    {
        final byte FORWARD_SOLIDUS = 47;    //  /
        final byte BACKWARDS_SOLIDUS = 92;  //  \

        boolean modified = false;
        byte[] bytes = Files.readAllBytes(filePath);
        for (int i = 0; i < bytes.length; i++)
        {
            if (bytes[i] == BACKWARDS_SOLIDUS)
            {
                modified = true;
                bytes[i] = FORWARD_SOLIDUS;
            }
        }

        if (modified)
        {
            Files.write(filePath, bytes);
        }
    }
}
