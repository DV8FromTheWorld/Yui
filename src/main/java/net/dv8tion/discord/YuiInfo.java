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

import net.dv8tion.discord.util.Downloader;
import net.dv8tion.discord.util.Version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YuiInfo
{
    public static final String LATEST_BUILD_ROOT = "https://ci.dv8tion.net/job/Yui/lastCompletedBuild/artifact/build/libs/";
    public static final Version VERSION = new Version(
            "@versionMajor@",
            "@versionMinor@",
            "@versionRevision@",
            "@versionBuild@"
    );
    public static final String URL_REGEX = "\\<a href=\"Yui-[0-9]*\\.[0-9]*\\.[0-9]*_[0-9]*-withDependencies\\.jar\">(Yui-[0-9]*\\.[0-9]*\\.[0-9]*_[0-9]*-withDependencies\\.jar)\\<\\/a\\>";
    private static final String VERSION_REGEX = "Yui-([0-9]*\\.[0-9]*\\.[0-9]*_[0-9]*)-withDependencies\\.jar";

    public static String getLatestBuildUrl()
    {
        String page = Downloader.webpage(LATEST_BUILD_ROOT);
        Pattern urlPattern = Pattern.compile(URL_REGEX);
        Matcher urlMatcher = urlPattern.matcher(page);
        if (urlMatcher.find())
        {
            return LATEST_BUILD_ROOT + urlMatcher.group(1);
        }
        else
            throw new RuntimeException("Could not find info at latest build URL.");
    }

    public static Version getLatestBuildVersion()
    {
        String url = getLatestBuildUrl();
        Pattern verPattern = Pattern.compile(VERSION_REGEX);
        Matcher verMatcher = verPattern.matcher(url);
        if (verMatcher.find())
        {
            return new Version(verMatcher.group(1));
        }
        else
            throw new RuntimeException("Could not find latest version from URL. URL: " + url);
    }

    public static boolean hasNewBuild()
    {
        return VERSION.olderThan(getLatestBuildVersion());
    }
}
