/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vtcc.interpreter;

import eu.mihosoft.vtcc.interpreter.util.CInterpreterImpl;
import java.io.File;
import java.io.PrintStream;

/**
 * Executes native tcc which interprets C code (supports many C99 features).
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface CInterpreter {

    /**
     * Destroys the currently running tcc process.
     */
    void destroy();

    /**
     * Returns the process of the current tcc execution.
     * @return the process of the current tcc execution
     */
    Process getProcess();

    /**
     * Returns the working directory
     * @return the working directory
     */
    File getWorkingDirectory();

    /**
     * Prints the tcc output to the specified print streams.
     * @param out standard output stream
     * @param err error output stream
     * @return this interpreter
     */
    CInterpreter print(PrintStream out, PrintStream err);

    /**
     * Prints the tcc output to the standard output.
     * @return this interpreter
     */
    CInterpreter print();

    /* TODO 19.02.2018 add support for external headers and libraries
    CInterpreter addIncludeFolder(File folder);

    CInterpreter addLibraryFolder(File folder);

    CInterpreter addLibrary(String libName);
    */

    /**
     * Waits until the tcc process terminates.
     * @return this interpreter
     */
    CInterpreter waitFor();
    
    /**
     * Executes tcc with the specified C script.
     *
     * @param wd working directory (currently ignored)
     * @param script script code that shall be executed
     * @return this interpreter
     */
    static CInterpreter execute(File wd, String script) {
        return CInterpreterImpl.execute(wd, script);
    }
    
    /**
     * Executes tcc with the specified C script.
     * 
     * @param wd working directory (currently ignored)
     * @param script C script that shall be executed
     * @return this interpreter
     */
    static CInterpreter execute(File wd, File script) {
        return CInterpreterImpl.execute(wd, script);
    }
    
    /**
     * Executes tcc with the specified arguments.
     *
     * @param arguments arguments
     * @param wd working directory  (currently ignored)
     * @return tcc process
     */
    static Process execute(File wd, String... arguments) {
        return CInterpreterImpl.execute(false, wd, arguments);
    }
    /**
     * Executes tcc with the specified C script.
     *
     * @param script script code that shall be executed
     * @return this interpreter
     */
    static CInterpreter execute(String script) {
        return CInterpreterImpl.execute(null, script);
    }

    /**
     * Executes tcc with the specified C script.
     *
     * @param script C script that shall be executed
     * @return this interpreter
     */
    static CInterpreter execute(File script) {
        return CInterpreterImpl.execute(null, script);
    }

    /**
     * Executes tcc with the specified arguments.
     *
     * @param arguments arguments
     * @return tcc process
     */
    static Process execute(String... arguments) {
        return CInterpreterImpl.execute(false, null, arguments);
    }

    /**
     * Returns the tcc installation folder.
     *
     * @return the tcc installation folder
     */
    static File getTCCInstallationFolder() {
        return CInterpreterImpl.getTCCRootPath();
    }
}
