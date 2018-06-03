package eu.mihosoft.vtcc.interpreter.util;

import eu.mihosoft.vtcc.interpreter.CInterpreter;
import eu.mihosoft.vtcc.tccdist.TCCDist;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class CInterpreterImpl implements CInterpreter {

    private static File executableFile;
    private static File tccRootPath;
    private final Process tccProcess;
    private static boolean initialized;
    private StreamGobbler errorGobbler;
    private StreamGobbler stdGobbler;

    private final List<File> includeFolders = new ArrayList<>();
    private final List<File> libraryFolders = new ArrayList<>();
    private final List<String> libraries = new ArrayList<>();

    static {
        // static init
    }
    private final File wd;

    private CInterpreterImpl(Process proc, File wd) {
        this.tccProcess = proc;
        this.wd = wd;
    }

    /**
     * Initializes property folder and executable.
     */
    private static void initialize() {

        // already initialized: we don't do anything
        if (initialized) {
            return;
        }

        try {

            Path confDir
                    = Paths.get(System.getProperty("user.home"), ".vtcc").
                    toAbsolutePath();
            Path distDir = Paths.get(confDir.toString(), "tcc-dist");
            File base = confDir.toFile();

            if (!Files.exists(confDir)) {
                Files.createDirectory(confDir);
            }

            if (!Files.exists(distDir)) {
                Files.createDirectory(distDir);
            }

            ConfigurationFile confFile
                    = IOUtil.newConfigurationFile(new File(base, "config.xml"));
            confFile.load();
            String timestamp = confFile.getProperty("timestamp");
            File tccFolder = distDir.toFile();//, "tcc");

            String timestampFromDist;

            try {
                Class<?> buildInfoCls = Class.forName("eu.mihosoft.vtcc.tccdist.BuildInfo");
                Field timestampFromDistField = buildInfoCls.getDeclaredField("TIMESTAMP");
                timestampFromDistField.setAccessible(true);
                timestampFromDist = (String) timestampFromDistField.get(buildInfoCls);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CInterpreterImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(
                        "TCC distribution for \"" + VSysUtil.getPlatformInfo()
                                + "\" not available on the classpath!", ex);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(CInterpreterImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(
                        "TCC distribution for \"" + VSysUtil.getPlatformInfo()
                                + "\" does not contain valid build info!", ex);
            }

            // if no previous timestamp exists or if no tcc folder exists
            if (timestamp == null || !tccFolder.exists()) {
                System.out.println("ts: " + timestamp + ", " + tccFolder);
                System.out.println(
                        " -> installing tcc to \"" + distDir + "\"");
                TCCDist.extractTo(distDir.toFile());
                confFile.setProperty("timestamp", timestampFromDist);
                confFile.save();
            } else // we need to update the tcc distribution
                if (!Objects.equals(timestamp, timestampFromDist)) {
                    System.out.println(
                            " -> updating tcc in \"" + distDir + "\"");
                    System.out.println(" --> current version: " + timestamp);
                    System.out.println(" --> new     version: " + timestampFromDist);
                    TCCDist.extractTo(distDir.toFile());
                    confFile.setProperty("timestamp", timestampFromDist);
                    confFile.save();
                } else {
                /*System.out.println(
                        " -> tcc up to date in \"" + distDir + "\""
                );*/
                }

            executableFile = getExecutablePath(distDir);

        } catch (IOException ex) {
            Logger.getLogger(CInterpreterImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        initialized = true;
    }

    @Override
    public CInterpreterImpl print(PrintStream out, PrintStream err) {
        errorGobbler = new StreamGobbler(err, tccProcess.getErrorStream(), "");
        errorGobbler.start();
        stdGobbler = new StreamGobbler(out, tccProcess.getInputStream(), "");
        stdGobbler.start();

        return this;
    }

    @Override
    public CInterpreterImpl print() {
        errorGobbler = new StreamGobbler(System.err, tccProcess.getErrorStream(), "");
        errorGobbler.start();

        stdGobbler = new StreamGobbler(System.out, tccProcess.getInputStream(), "");
        stdGobbler.start();

        return this;
    }

    @Override
    public CInterpreterImpl waitFor() {
        try {
            tccProcess.waitFor();
            if(errorGobbler!=null) {
                errorGobbler.join();
                stdGobbler.join();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CInterpreterImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Cannot wait until process is finished", ex);
        }

        return this;
    }

    /**
     * Executes TCC with the specified script.
     *
     * @param wd working directory (currently ignored)
     * @param script script that shall be executed
     * @return this shell
     */
    public static CInterpreterImpl execute(File wd, String script) {
        File tmpDir;
        File scriptFile;
        try {
            tmpDir = Files.createTempDirectory("tcc-script-tmp").toFile();
            scriptFile = new File(tmpDir, "code.c");
            Files.write(scriptFile.toPath(), script.getBytes("UTF-8"));
        } catch (IOException ex) {
            Logger.getLogger(CInterpreterImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Cannot execute script due to io exception", ex);
        }

        return execute(tmpDir, scriptFile);
    }

    /**
     * Executes tcc with the specified script.
     *
     * @param wd working directory (currently ignored)
     * @param script script that shall be executed
     * @return this shell
     */
    public static CInterpreterImpl execute(File wd, File script) {

        initialize();

        Path scriptFile;

        try {
            scriptFile = Files.createTempFile("tcc_script", ".c");

            String scriptCode = new String(
                    Files.readAllBytes(script.toPath()), "UTF-8");

            Files.write(scriptFile,
                    scriptCode.getBytes(Charset.forName("UTF-8")));

        } catch (IOException ex) {
            Logger.getLogger(CInterpreterImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Cannot create tmp script-file", ex);
        }

        String[] args;

        if(VSysUtil.isWindows()) {

            String tccFolder = executableFile.getAbsoluteFile().getParent();

            args = new String[]{"-B"+tccFolder+"\\lib\\;"+tccFolder,"-L"+tccFolder+"\\lib\\;"+tccFolder,
                    "-I" +tccFolder+"\\include\\libc;"+tccFolder+"\\include;" + tccFolder + "\\lib\\tcc\\include\\",
                    "-nostdinc", /*TODO 19.02.2018 does not work "-nostdlib",*/
                    "-llibtcc",
                    "-run", scriptFile.toAbsolutePath().toString()};

        } else {

            String tccFolder = executableFile.getAbsoluteFile().getParentFile().getParent();

            args = new String[]{"-B"+tccFolder+"/lib/tcc/",
                    "-I" +tccFolder+"/include/libc:"+tccFolder+"/include:" + tccFolder + "/lib/tcc/include/",
                    "-nostdinc", /*TODO 03.06.2018 does not work "-nostdlib" on static tcc binary and on macos,*/
                    "-run", scriptFile.toAbsolutePath().toString()};
        }

        Process proc = execute(false, wd, args);

        return new CInterpreterImpl(proc, wd);
    }


    @Override
    public File getWorkingDirectory() {
        return wd;
    }

    /**
     * Calls tcc with the specified arguments.
     *
     * @param arguments arguments
     * @param wd working directory (currently ignored)
     * @param waitFor indicates whether to wait for process execution
     * @return tcc process
     */
    public static Process execute(boolean waitFor, File wd, String... arguments) {

        initialize();

        if (arguments == null || arguments.length == 0) {
            arguments = new String[]{"--help"};
        }

        String[] cmd = new String[arguments.length + 1];

        cmd[0] = executableFile.getAbsolutePath();

        for (int i = 1; i < cmd.length; i++) {
            cmd[i] = arguments[i - 1];
        }

        // System.out.println(">> final tcc command: " + String.join(" ", cmd));
        // System.out.println(" -> cmd args:\n   -> " + String.join("\n   -> ", cmd));

        Process proc = null;

        try {
            if(wd==null) {
                proc = Runtime.getRuntime().exec(cmd);
            } else {
                proc = Runtime.getRuntime().exec(cmd, null, wd);
            }
            if (waitFor) {
                proc.waitFor();
            }
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException("Error while executing tcc", ex);
        }

        return proc;
    }

    @Override
    public Process getProcess() {
        return tccProcess;
    }

    /**
     * Destroys the currently running tcc process.
     */
    @Override
    public void destroy() {
        if (tccProcess != null) {
            tccProcess.destroy();
        }
    }

    /**
     * Returns the path to the tcc executable. If the executable has not
     * been initialized this will be done as well.
     *
     * @return the path to the tcc executable
     */
    private static File getExecutablePath(Path dir) {

        if (!VSysUtil.isOsSupported()) {
            throw new UnsupportedOperationException(
                    "The current OS is not supported: "
                            + System.getProperty("os.name"));
        }

        if (executableFile == null || !executableFile.isFile()) {

            tccRootPath = dir.toFile();// new File(dir.toFile(), "tcc");

            String executableName;

            if (VSysUtil.isWindows()) {
                executableName = "tcc.exe";
            } else {
                executableName = "bin/tcc";
            }

            executableFile = new File(tccRootPath, executableName);

            if (!VSysUtil.isWindows()) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{
                            "chmod", "u+x",
                            executableFile.getAbsolutePath()
                    });

                    InputStream stderr = p.getErrorStream();

                    BufferedReader reader
                            = new BufferedReader(
                            new InputStreamReader(stderr));

                    String line;

                    while ((line = reader.readLine()) != null) {
                        System.out.println("Error: " + line);
                    }

                    p.waitFor();
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(CInterpreterImpl.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        }

        return executableFile;
    }

    /**
     * Unzips specified source archive to the specified destination folder. If
     * the destination directory does not exist it will be created.
     *
     * @param archive archive to unzip
     * @param destDir destination directory
     * @throws IOException
     */
    public static void unzip(File archive, File destDir) throws IOException {
        IOUtil.unzip(archive, destDir);
    }

    /**
     * Saves the specified stream to file.
     *
     * @param in stream to save
     * @param f destination file
     * @throws IOException
     */
    public static void saveStreamToFile(InputStream in, File f) throws IOException {
        IOUtil.saveStreamToFile(in, f);
    }

    public static File getTCCRootPath() {
        initialize();

        return tccRootPath;
    }


}
// based on http://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki

class StreamGobbler extends Thread {

    private  volatile InputStream is;
    private  volatile String prefix;
    private  volatile PrintStream pw;

    StreamGobbler(PrintStream pw, InputStream is, String prefix) {
        this.is = is;
        this.prefix = prefix;
        this.pw = pw;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                pw.println(prefix + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

}