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

    /**
     * Waits until the tcc process terminates.
     * @return this interpreter
     */
    CInterpreter waitFor();
    
    /**
     * Executes tcc with the specified C script.
     *
     * @param wd working directory
     * @param script script code that shall be executed
     * @return this interpreter
     */
    public static CInterpreter execute(File wd, String script) {
        return CInterpreterImpl.execute(wd, script);
    }
    
    /**
     * Executes tcc with the specified C script.
     * 
     * @param wd working directory
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
     * @param wd working directory
     * @return tcc process
     */
    public static Process execute(File wd, String... arguments) {
        return CInterpreterImpl.execute(false, wd, arguments);
    }
    
}
