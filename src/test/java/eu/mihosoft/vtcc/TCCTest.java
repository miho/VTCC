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

import eu.mihosoft.vtcc.interpreter.util.VSysUtil;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class TCCTest {

    @Test
    public void executeExampleScriptTest() throws IOException {
        
        // show platform (os+arch)
        String osAndArch = VSysUtil.getOSName() + ", " + VSysUtil.getArchName();
        
        System.out.println("OS-Info: "+ osAndArch);

        String code = "" +
                "#include <stdio.h>\n" +
                "                  \n"+
                "int main(int argc, char* argv[]) {\n" +
                "  printf(\"Hello, World!\\n\");\n"+
                "  return 0;\n"+
                "}";

//        code = "#include <stdio.h>\n" +
//                "#include <unistd.h>\n" +
//                "int main(int argc, char* argv[]) {\n" +
//                "   char cwd[1024];\n" +
//                "   if (getcwd(cwd, sizeof(cwd)) != NULL)\n" +
//                "       printf(\"Current working dir: %s\\n\", cwd);\n" +
//                "   else\n" +
//                "       perror(\"getwd() error\");\n" +
//                "   return 0;"+
//                "}";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        CInterpreter.debug();

        boolean noError = CInterpreter.execute(code).
                print(ps, System.err).waitFor().
                getProcess().exitValue() == 0;

        String output = new String(baos.toByteArray(),
                StandardCharsets.UTF_8);

        ps.close();

        System.out.println(output);
        
        Assert.assertTrue("Execution must not fail", noError);

        String lineEnding = System.getProperty("line.separator");

        Assert.assertEquals("Hello, World!"+lineEnding, output);
    }
}
