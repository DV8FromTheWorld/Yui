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
package net.dv8tion.discord.util;

public class Version implements Comparable<Version>
{
    private final int major;
    private final int minor;
    private final int revision;
    private final int build;

    public Version(String major, String minor, String revision, String build)
    {
        this.major = !major.contains("@") ? Integer.parseInt(major) : -1;
        this.minor = !minor.contains("@") ? Integer.parseInt(minor) : -1;
        this.revision = !revision.contains("@") ? Integer.parseInt(revision) : -1;
        this.build = !build.contains("@") ? Integer.parseInt(build) : -1;
    }

    public Version(String version)
    {
        this(
                version.split("\\.")[0],               //Major
                version.split("\\.")[1],               //Minor
                version.split("\\.")[2].split("_")[0], //Revision
                version.split("\\.")[2].split("_")[1]);//Build
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getRevision()
    {
        return revision;
    }

    public int getBuild()
    {
        return build;
    }

    public boolean newerThan(Version v)
    {
        return compareTo(v) > 0;
    }

    public boolean olderThan(Version v)
    {
        return compareTo(v) < 0;
    }

    @Override
    public String toString()
    {
        return String.format("%d.%d.%d_%d", major, minor, revision, build);
    }

    @Override
    public int compareTo(Version o)
    {
        if (equals(o))
            return 0;

        if (getMajor() > o.getMajor())
            return 1;
        else if (getMajor() < o.getMajor())
            return -1;

        if (getMinor() > o.getMinor())
            return 1;
        else if (getMinor() < o.getMinor())
            return -1;

        if (getRevision() > o.getRevision())
            return 1;
        else if (getRevision() < o.getRevision())
            return -1;

        if (getBuild() > o.getBuild())
            return 1;
        else if (getBuild() < o.getBuild())
            return  -1;

        throw new RuntimeException("Somehow the Version isn't greater, equal or less?");
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Version))
            return false;

        Version vO = (Version) o;
        return major == vO.major && minor == vO.minor && revision == vO.revision && build == vO.build;
    }
}
