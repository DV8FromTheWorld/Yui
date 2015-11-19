package net.dv8tion.discord.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.message.MessageBuilder;
import net.dv8tion.discord.Downloader;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.StringUtils;

public class PullCommand extends Command
{
    private String gitRepoUrl;
    private String javacPath;
    private Boolean javacExists;

    public PullCommand(String gitRepoUrl, String javaJDKPath)
    {
        this.gitRepoUrl = gitRepoUrl;
        javacExists = null;

        if (!gitRepoUrl.endsWith("/"))
        {
            this.gitRepoUrl += "/archive/master.zip";
        }
        else
        {
            this.gitRepoUrl +="archive/master.zip";
        }

        if (javaJDKPath != null && !javaJDKPath.isEmpty())
        {
            if (!javaJDKPath.equals("javac"))
            {
                this.javacPath = "\"" + javaJDKPath + (javaJDKPath.endsWith("/") ? "javac" : "/javac") + "\"";
            }
            else
            {
                this.javacPath = javaJDKPath;
            }
            this.javacExists = testJDKPath();
        }
        else
        {
            this.javacPath = javaJDKPath;   //Transfers the empty string for checking.
            this.javacExists = false;
        }
    }

    @Override
    public void onChat(UserChatEvent e)
    {
        String[] args = e.getMsg().toString().split(" ");
        if (!aliases().contains(args[0]))
        {
            return;
        }

        //TODO: Check permission - admin

        if (javacPath.isEmpty())
        {
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": This command is disabled because no Java v1.8+ JDK was provided. Provide one in the Config to enable.")
                .addString(" JDK path can either be the File path to the JDK's bin folder,\n")
                .addString("**Example:** C:/Program Files/Java/jdk1.8.0_65/bin\n")
                .addString("or if you installed the JDK to your OS's PATH, just 'javac'")
                .build());
            return;
        }

        //If the JDK Path has errors.
        if (!javacExists)
        {
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": This command is disabled because the path to the Java v1.8 JDK is incorrect.\n")
                .addString(" The following command will not properly execute the Java compiler (javac):\n")
                .addString("**Command:** " + javacPath + "\n")
                .addString(javacPath.equals("javac") ? "Perhaps you didn't properly set the PATH variable for the JDK?\n" : "")
                .addString("Please fix this in the Config. If the command does work in your command line, contact the Developer.'")
                .build());
            return;
        }

        try
        {
            ZipFile zip = new ZipFile(Downloader.file(gitRepoUrl, "./source/Master.zip"));
            zip.extractAll("./source/");

            final String rootDir = "./source/Discord-Bot-master/";
            final File source = new File(rootDir + "src/");
            final File sourcePathsFile = new File("./source/SourcePaths.txt");
            final File classPathFile = new File(rootDir + ".classpath");
            final File binFolder = new File("./source/bin/");

            //If we've pulled before, clean up the /bin/ files from last compile.
            if (binFolder.exists())
                binFolder.delete();
            binFolder.mkdir();

            //Recursively gets all .java file paths from the Repo's /src/ folder, prints them to the SourcePaths.txt file.
            PrintWriter filesWriter = new PrintWriter(sourcePathsFile, "UTF-8");
            filesWriter.println(".");   //Needed or javac will skip the first path defined in SourcePaths.txt
            getSourcePaths(source, filesWriter);
            filesWriter.flush();
            filesWriter.close();

            //Looks inside the Repo's .classpath file and gets the paths of all required libs.
            String classpath = getLibraryPaths(classPathFile, rootDir);
            String[] compileCommand = new String[] {
                    javacPath,
                    "-cp", classpath,
                    "-d", binFolder.getPath(),
                    "-sourcepath", "@" + sourcePathsFile.getPath()
            };
            System.out.println(StringUtils.join(compileCommand, " ", 0, compileCommand.length));

            ProcessBuilder pb = new ProcessBuilder();
            pb.command(compileCommand);
            pb.inheritIO();
            Process compileProcess = pb.start();
            compileProcess.waitFor();
            if (compileProcess.exitValue() == 0)
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": Successfully Compiled.")
                    .build());
            }
            else
            {
                e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": Compile failed, exit value: " + compileProcess.exitValue() + "\n")
                .addString("**Command: **" + StringUtils.join(compileCommand, " ", 0, compileCommand.length))
                .build());
            }
        }
        catch (IOException | ZipException | InterruptedException e1)
        {
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(String.format(": Error attempt to pull.\n**%s**: %s\nPlease check the console for stacktrace.",
                        e1.getClass().getName(),
                        e1.getMessage()))
                .build());
            e1.printStackTrace();
        }
    }

    @Override
    public List<String> aliases()
    {
        return Arrays.asList(new String[] {".pull", ".pull-reload"});
    }

    @Override
    public String commandDescription()
    {
        return "Used to pull source code from the Bot's git repo and compile it";
    }

    @Override
    public String helpMessage()
    {
        return null;
    }

    private void getSourcePaths(File root, PrintWriter writer)
    {
        if (root.getPath().endsWith(".java"))
            writer.println(root.getPath());
        if (root.isDirectory())
        {
            for (File f : root.listFiles())
            {
                getSourcePaths(f, writer);
            }
        }
    }

    private String getLibraryPaths(File classPathFile, String rootDir) throws IOException
    {
        String classpaths = "";
        List<String> contents = Files.readAllLines(classPathFile.toPath(), Charset.defaultCharset());
        for (String line : contents)
        {
            if (line.contains("kind=\"lib\""))
            {
                Pattern p = Pattern.compile("(?<=path=\").*?(?=\")");
                Matcher m = p.matcher(line);
                if (m.find())
                    classpaths += rootDir + m.group() + ";";
            }
        }
        return classpaths;
    }

    private boolean testJDKPath()
    {
        if (javacExists != null)
            return javacExists;
        try
        {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(javacPath);
            Process process = builder.start();
            process.waitFor();

            this.javacExists = process.exitValue() == 2;    //2 is the exit code of Javac when no file is provided. This is what we expect.
        }
        catch (IOException | InterruptedException e)
        {
            this.javacExists = false;
        }
        return javacExists;
    }
}
