package eu.mihosoft.vtcc.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * StringPrintStream collects printed content into a string.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public final class StringPrintStream extends PrintStream {
    private ByteArrayOutputStream baos;
    private PrintStream ps;


    private StringPrintStream(ByteArrayOutputStream outputStream) {
        super(outputStream);
        baos = outputStream;
        ps = this;
    }

    /**
     * Creates a new print stream.
     */
    public StringPrintStream() {
        this(new ByteArrayOutputStream());
    }

    /**
     * Creates a new print stream.
     *
     * @return new print stream
     */
    public static StringPrintStream newInstance() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        StringPrintStream ps = new StringPrintStream(bs);

        return ps;
    }

    /**
     * Returns the contents of this print stream as string.
     *
     * @return the contents of this print stream as string
     */
    public String toString() {
        ps.flush();

        String output = new String(baos.toByteArray(),
                StandardCharsets.UTF_8);

        return output;
    }
}
