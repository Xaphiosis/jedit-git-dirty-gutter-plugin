# jedit-git-dirty-gutter-plugin
jEdit plugin for highlighting lines that have been changed since the last Git commit.

## Development Environment

### JDK

This project currently targets Java 7.  Therefore, it should be built using JDK 7 to avoid incompatible boot classpath warnings during compilation.  Ensure `JAVA_HOME` points to your JDK 7 installation, for example:

    $ export JAVA_HOME=~/Programs/jdk1.7.0_80

Also ensure this JDK appears before any other JDK in `PATH`:

    $ export PATH=$JAVA_HOME/bin:$PATH

### Eclipse

If using Gradle from within Eclipse, the Gradle build launch configurations use the default JDK in `PATH`.  Therefore, you should run Eclipse from the same terminal where you configured `JAVA_HOME` and `PATH` above.
