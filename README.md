# VTCC

[ ![Download](https://api.bintray.com/packages/miho/TCC/VTCC/images/download.svg) ](https://bintray.com/miho/TCC/VTCC/_latestVersion) [![Build status](https://ci.appveyor.com/api/projects/status/862o08c4bwgdyafp?svg=true)](https://ci.appveyor.com/project/miho/vtcc) [![Build Status](https://travis-ci.org/miho/VTCC.svg?branch=master)](https://travis-ci.org/miho/VTCC)

Compile and Run C code from Java via embedded [TCC](https://bellard.org/tcc/) Compiler (Works on Linux, Windows and macOS)

## Usage:

To execute C code, the following method can be used:
```java
// execute code with working directory "exampleDir"
String code = "#include <stdio.h>\n" +
              "int main(int argc, char* argv[]) {\n" +
              "  printf(\"Hello, World!\\n\");\n"+
              "  return 0;\n"+
              "}";
CInterpreter.execute(exampleDir, code);
```
To execute a specified C script, use:
```java
// execute main.c
CInterpreter.execute(new File("main.c"));
```
To print the output to given print streams, use the `print(...)` method:
```java
// execute main.c
CInterpreter.execute(new File("main.c")).print(System.out,System.err);
```
To wait until the execution has finished, use the `waitFor()` method:
```java
// execute main.c with working directory "exampleDir"
CInterpreter.execute(exampleDir, new File(exampleDir, "main.c")).waitFor();
```
The exit value can be accessed via:
```java
// execute main.c with working directory "exampleDir"
CInterpreter.execute(new File("main.c")).getProcess().exitValue();
```
To destroy the current process, the `destroy()`method can be used:
```java
// execute main.c
CInterpreter.execute(new File("main.c")).destroy();
```

## How to Build VTCC

### Requirements

- Java >= 1.8
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)

### IDE

Open the `VTCC` [Gradle](http://www.gradle.org/) project in your favourite IDE and build it
by calling the `assemble` task.

### Command Line

Navigate to the [Gradle](http://www.gradle.org/) project (e.g., `path/to/VTCC`) and enter the following command

#### Bash (Linux/OS X/Cygwin/other Unix-like CInterpreter)

    sh gradlew assemble
    
#### Windows (CMD)

    gradlew assemble
