package eu.mihosoft.vtcc.tccdist;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * UG distribution class.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class TCCDist {

    public static final String RESOURCE_PACKAGE = "/eu/mihosoft/vtcc/tccdist";

    public static String getResourcePath() {
        return RESOURCE_PACKAGE
                + "/" + VSysUtil.getPlatformSpecificPath();
    }

    /**
     *
     * @param f folder where the ug distribution shall be extracted to (folder
     * will be created if it does not exist)
     * @throws IOException
     */
    public static void extractTo(File f) throws IOException {
        f.mkdirs();

        Path tmpDir = Files.createTempDirectory("tccdist");
        File tccdist = new File(tmpDir.toFile(), "tcc-dist.zip");

        String resourceName
                = RESOURCE_PACKAGE
                + "/" + VSysUtil.getPlatformSpecificPath() + "tcc.zip";
        InputStream stream = TCCDist.class.getResourceAsStream(resourceName);

        if (stream == null) {

            System.err.println("TCC distribution for \""
                    + VSysUtil.getPlatformInfo()
                    + "\" not available on the classpath!");

            throw new RuntimeException(
                    "TCC distribution for \"" + VSysUtil.getPlatformInfo()
                    + "\" not available on the classpath!");
        }

        IOUtil.enableDebugging(true);
        IOUtil.saveStreamToFile(stream, tccdist);
        IOUtil.unzip(tccdist, f);
    }

    /**
     * extract the subdirectory from a jar on the classpath to the specified
     * target directory.
     *
     * Based on
     * https://coderanch.com/t/472574/java/extract-directory-current-jar
     *
     * @param classloader classloader
     * @param sourceDirectory directory (in a jar on the classpath) to extract
     * @param targetDirectory the location to extract to
     * @throws IOException if an IO exception occurs
     */
    private static void extractDirFromClassPath(ClassLoader classloader,
            String sourceDirectory,
            File targetDirectory) throws IOException {

        sourceDirectory = sourceDirectory.replace("//", "/");

        final URL dirURL = classloader.getResource(sourceDirectory);

        System.out.println("dir: " + dirURL);

        final String path = sourceDirectory.substring(1);

        if ((dirURL != null) && dirURL.getProtocol().equals("jar")) {

            final JarURLConnection jarConnection = (JarURLConnection) dirURL.openConnection();

            System.out.println("jarConnection is " + jarConnection);

            final ZipFile jar = jarConnection.getJarFile();

            final Enumeration< ? extends ZipEntry> entries = jar.entries(); // gives ALL entries in jar

            while (entries.hasMoreElements()) {

                final ZipEntry entry = entries.nextElement();

                final String name = entry.getName();
                // System.out.println( name );

                if (!name.startsWith(path)) {
                    // entry in wrong subdir -- don't copy
                    continue;
                }

                final String entryTail = name.substring(path.length());

                final File f = new File(targetDirectory, entryTail);
                targetDirectory.mkdirs();

                if (entry.isDirectory()) {
                    // if its a directory, create it

                    final boolean bMade = f.mkdir();

                    System.out.println((bMade ? "  creating " : "  unable to create ") + name);

                } else {

                    System.out.println("  writing  " + name);

                    try (
                            InputStream is = jar.getInputStream(entry);
                            OutputStream os = new BufferedOutputStream(new FileOutputStream(f))) {

                        final byte buffer[] = new byte[4096];

                        int readCount;
                        // write contents of 'is' to 'os'

                        while ((readCount = is.read(buffer)) > 0) {
                            os.write(buffer, 0, readCount);
                        }

                    }
                }

            }

        } else if (dirURL == null) {

            throw new IllegalStateException("can't find " + sourceDirectory + " on the classpath");

        } else {
            // not a "jar" protocol URL

            throw new IllegalStateException("don't know how to handle extracting from " + dirURL);
        }

    }
}
