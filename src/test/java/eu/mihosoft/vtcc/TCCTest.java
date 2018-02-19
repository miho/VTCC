/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vtcc;

import eu.mihosoft.vtcc.interpreter.CInterpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class TCCTest {

    @Test
    public void executeExampleScriptTest() throws IOException {

        String code = "" +
                "#include <stdio.h>\n" +
                "int main(int argc, char* argv[]) {\n" +
                "  printf(\"Hello, World!\\n\");\n"+
                "  return 0;\n"+
                "}";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        boolean noError = CInterpreter.execute(new File("./"), code).
                print(ps, System.err).waitFor().
                getProcess().exitValue() == 0;

        String output = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();

        System.out.println(output);
        
        Assert.assertTrue("Execution must not fail", noError);

        String lineEnding = System.getProperty("line.separator");

        Assert.assertEquals("Hello, World!"+lineEnding, output);

    }
}
