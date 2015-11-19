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
import net.dv8tion.discord.Bot;
import net.dv8tion.discord.Downloader;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class PullCommand extends Command
{
    private String gitRepoUrl;
    private String javaJDKPath;
    private Boolean javaJDKExists;

    private final String JDK_JAVAC_COMMAND = "javac";
    private final String JDK_JAR_COMMAND = "jar";
    private final int EXITCODE_SUCCESS = 0;         //Exit code when a proccess finishes with no problems.
    private final int EXITCODE_FILE_NOT_FOUND = 2;  //This is the exit code the JavaJDK uses to specify that it was run with no arguments / files provided.

    public PullCommand(String gitRepoUrl, String javaJDKPath)
    {
        this.gitRepoUrl = gitRepoUrl;
        javaJDKExists = null;

        if (!gitRepoUrl.endsWith("/"))
        {
            this.gitRepoUrl += "/archive/master.zip";
        }
        else
        {
            this.gitRepoUrl +="archive/master.zip";
        }
        if (!javaJDKPath.isEmpty() && !javaJDKPath.endsWith("/"))
            this.javaJDKPath = javaJDKPath + "/";
        else
            this.javaJDKPath = javaJDKPath;
        this.javaJDKExists = testJDKExists();
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

        //If the JDK Path has errors.
        if (!javaJDKExists)
        {
            e.getGroup().sendMessage(new MessageBuilder()
                .addUserTag(e.getUser(), e.getGroup())
                .addString(": __This command is disabled because no Java JDK install could be found.__\n")
                .addString("One following commands will not properly execute the Java compiler:\n")
                .addString("**Command:** " + javaJDKPath + JDK_JAVAC_COMMAND + "\n")
                .addString("**Command:** " + javaJDKPath + JDK_JAR_COMMAND + "\n")
                .addString("You will need to download and install the Java JDK to use this command.\n")
                .addString("After installing the JDK, either add the JDK to your Operating System's PATH variable or")
                .addString(" provide the path to the JDK's /bin/ folder in the Config file.\n")
                .addString("**Example:** C:/Program Files/Java/jdk1.8.0_65/bin\n")
                .build());
            return;
        }

        try
        {
            ZipFile zip = new ZipFile(Downloader.file(gitRepoUrl, "./source/Master.zip"));
            zip.extractAll("./source/");

            final File newJarFile = new File("./source/" + Bot.getThisJarFile().getName());
            final String rootDir = "./source/Discord-Bot-master/";
            final File source = new File(rootDir + "src/");
            final File sourcePathsFile = new File("./source/SourcePaths.txt");
            final File classPathFile = new File(rootDir + ".classpath");
            final File binFolder = new File("./source/bin/");
            final File extractedFolder = new File("./source/extracted/");

            //If we've pulled before, clean up the /bin/ files from last compile.
            FileUtils.deleteDirectory(binFolder);
            binFolder.mkdir();

            //If we've pulled the libs before, clean up the /extracted/ files from last compile.
            FileUtils.deleteDirectory(extractedFolder);
            extractedFolder.mkdir();

            //Recursively gets all .java file paths from the Repo's /src/ folder, prints them to the SourcePaths.txt file.
            PrintWriter filesWriter = new PrintWriter(sourcePathsFile, "UTF-8");
            filesWriter.println(".");   //Needed or javac will skip the first path defined in SourcePaths.txt
            getSourcePaths(source, filesWriter);
            filesWriter.flush();
            filesWriter.close();

            //Looks inside the Repo's .classpath file and gets the paths of all required libs.
            String classpath = getLibraryPaths(classPathFile, rootDir);
            String[] compileCommand = new String[] {
                    javaJDKPath + JDK_JAVAC_COMMAND,
                    "-cp", classpath,
                    "-d", binFolder.getPath(),
                    "-sourcepath", "@" + sourcePathsFile.getPath()
            };

            //Create a process to compile the Repo's /src/ *.java files, monitor the process.
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(compileCommand);
            pb.inheritIO();
            Process compileProcess = pb.start();
            compileProcess.waitFor();
            if (compileProcess.exitValue() == EXITCODE_SUCCESS)
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": Successfully Compiled. Packaging libraries into jar...")
                    .build());
            }
            else
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": Compile failed, exit value: " + compileProcess.exitValue() + "\n")
                    .addString("**Command: **" + StringUtils.join(compileCommand, " ", 0, compileCommand.length))
                    .build());
                return;
            }

            //Extract the files from inside each provided lib and place them in ./sources/extracted/
            for (String libPath : classpath.split(";"))
            {
                ZipFile libFile = new ZipFile(libPath);
                libFile.extractAll(extractedFolder.getPath());
            }

            //Create the command to package the libraries into the new Jar.
            String[] jarLibsCommand = new String[] {
                    javaJDKPath + JDK_JAR_COMMAND,
                    "cf", newJarFile.getPath(),         //c : create new jar, f: filename.
                    "-C", extractedFolder.getPath(),    //-C: switch to provided directory.
                    "."                                 //. : Get all files in this directory, recursively.
            };

            //Create command to package the extracted libraries files in ./source/extracted/ into the jar file.
            pb = new ProcessBuilder();
            pb.command(jarLibsCommand);
            Process jarLibsProcess = pb.start();
            jarLibsProcess.waitFor();
            if (jarLibsProcess.exitValue() == EXITCODE_SUCCESS)
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": Successfully packaged libraries. Packaging compiled src into jar...")
                    .build());
            }
            else
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": Unable to package the Repo's extracted libraries, exit value: " + compileProcess.exitValue() + "\n")
                    .addString("**Command: **" + StringUtils.join(jarLibsCommand, " ", 0, compileCommand.length))
                    .build());
                return;
            }

            //Create command to package the compiled code in ./source/bin/ into the jar file.
            String[] jarSrcCommand = new String[] {
                    "jar",
                    "uef", "net.dv8tion.discord.Bot", newJarFile.getPath(), //c : create new jar, e: classpath of main, f: filename.
                    "-C", binFolder.getPath(),  //-C: switch to provided directory.
                    "."                         //. : Get all files in this directory, recursively.
            };

            //Takes the code compiled into ./source/bin/ and packages it into the Jar.
            pb = new ProcessBuilder();
            pb.command(jarSrcCommand);
            Process jarSrcProcess = pb.start();
            jarSrcProcess.waitFor();
            if (jarSrcProcess.exitValue() == EXITCODE_SUCCESS)
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": Successfully packaged all files. Jar creation was successful!")
                    .build());
            }
            else
            {
                e.getGroup().sendMessage(new MessageBuilder()
                    .addUserTag(e.getUser(), e.getGroup())
                    .addString(": Unable to package the Repo's compiled src, exit value: " + compileProcess.exitValue() + "\n")
                    .addString("**Command: **" + StringUtils.join(jarSrcCommand, " ", 0, compileCommand.length))
                    .build());
                return;
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

    private boolean testJDKExists()
    {
        if (javaJDKExists != null)
            return javaJDKExists;
        try
        {
            //Checks if running the 'javac' command works with the path provided (or PATH variable).
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(javaJDKPath + JDK_JAVAC_COMMAND);
            Process javacTestProcess = builder.start();
            javacTestProcess.waitFor();
            if (javacTestProcess.exitValue() != EXITCODE_FILE_NOT_FOUND)
            {
                this.javaJDKExists = false;
                return false;
            }

            //Checks if running the 'jar' command works with the path provided (or PATH variable).
            builder = new ProcessBuilder();
            builder.command(javaJDKPath + JDK_JAVAC_COMMAND);
            Process jarTestProcess = builder.start();
            jarTestProcess.waitFor();
            if (jarTestProcess.exitValue() != EXITCODE_FILE_NOT_FOUND)
            {
                this.javaJDKExists = false;
                return false;
            }
            this.javaJDKExists = true;
            return true;
        }
        catch (IOException | InterruptedException e)
        {
            this.javaJDKExists = false;
            return false;
        }
    }
}
