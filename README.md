# Notes on this (xsymbol) fork

This fork uses lcm.XSymbolSubst from an xsymbol-enabled DirtyGutter plugin to
translate xsymbols in file to unicode, which are already translated in the
buffer in the `UTF-8-Isabelle` encoding. Not doing this results in spurious
line change indicators.

This requires an xsymbol-enabled DirtyGutter plugin (e.g. version
`0.4-xsymbol`).

## Compiling in 2024

Attempts to build this plugin by someone very inexperienced with gradle,
eclipse, and so on many years after the last plugin update resulted in many
difficulties. The project was heavily engineered to include many tests, none of
whose frameworks seem to work properly anymore, and there were difficulties
locating build dependencies.

In order to obtain a compiled jar file, `build.gradle` was modified to accept
jars in a `libs` subfolder to fulfill the missing dependencies:
* `DirtyGutter.jar` (xsymbol-enabled DirtyGutter plugin mentioned above)
* `jedit.jar` (JEdit version 5.7pre1)
* `CommonControls.jar` (version 1.7.4 unmodified plugin obtained from JEdit website)
* `GitPlugin.jar` (version 0.8 unmodified plugin obtained from JEdit website)

With those in place, a "just give me the jar" compilation goes like this:
```bash
# Use Java 11
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
rm -rf build
# Avoid all tests, which while nice, don't seem to work properly anymore.
gradle build -x compileTestGroovy -x compileIntegrationTestGroovy -x compileAcceptanceTestGroovy -x checkstyleMain -x compileSmokeTestGroovy -x pmdMain -x pmdTest -x smokeTest -x findbugsMain
```

The above has been included as a `minimal_build.sh` script. A more experienced
Java developer could update the breaking dependencies / test frameworks in the
canonical way, but this fork is a drive-thru minimal change to functionality.

# Original README follows below:

# jEdit Git DirtyGutter Plugin

[![Build Status](https://travis-ci.org/ssoloff/jedit-git-dirty-gutter-plugin.svg?branch=master)](https://travis-ci.org/ssoloff/jedit-git-dirty-gutter-plugin)
[![Coverage Status](https://coveralls.io/repos/ssoloff/jedit-git-dirty-gutter-plugin/badge.svg?branch=master&service=github)](https://coveralls.io/github/ssoloff/jedit-git-dirty-gutter-plugin?branch=master)
[![Download](https://api.bintray.com/packages/ssoloff/maven/io.github.ssoloff%3Ajedit-git-dirty-gutter-plugin/images/download.svg)](https://bintray.com/ssoloff/maven/io.github.ssoloff%3Ajedit-git-dirty-gutter-plugin/_latestVersion)

A jEdit plugin that adds highlights in the gutter for lines that have been changed since the last Git commit.

## Installation

Start jEdit and open the Plugin Manager dialog (**Plugins > Plugin Manager...**).  Activate the **Install** tab.  Install the following prerequisite plugins, if necessary:

- `DirtyGutter`
- `Git Plugin`

Open a new terminal window and download the [latest version](https://bintray.com/ssoloff/maven/io.github.ssoloff%3Ajedit-git-dirty-gutter-plugin/_latestVersion) of the plugin binary distribution (either `jedit-git-dirty-gutter-plugin-*-bin.tar.gz` or `jedit-git-dirty-gutter-plugin-*-bin.zip`).  Extract the contents of the archive to the [jEdit plugin directory](http://plugins.jedit.org/install.php).  For example, to install the plugin in the default settings directory on Unix:

    $ tar xzf jedit-git-dirty-gutter-plugin-*-bin.tar.gz -C ~/.jedit/jars

Switch back to jEdit and activate the **Manage** tab of the Plugin Manager.  Enable the `Git DirtyGutter` plugin.  Close the Plugin Manager dialog.

Open the Plugin Options dialog (**Plugins > Plugin Options...**).  Activate the **DirtyGutter** node.  Change the **Dirty line provider** to **Git**.  Close the Plugin Options dialog.

**Note:** At this time, restarting jEdit after completing the above steps is necessary due to an apparent limitation of the `DirtyGutter` plugin.

## Development Environment

### JDK

This project currently targets Java 7.  Therefore, it should be built using JDK 7 to avoid incompatible boot classpath warnings during compilation.  Ensure `JAVA_HOME` points to your JDK 7 installation, for example:

    $ export JAVA_HOME=~/Programs/jdk1.7.0_80

Also ensure this JDK appears before any other JDK in `PATH`:

    $ export PATH=$JAVA_HOME/bin:$PATH

### Eclipse

If using Gradle from within Eclipse, the Gradle build launch configurations use the default JDK in `PATH`.  Therefore, you should run Eclipse from the same terminal where you configured `JAVA_HOME` and `PATH` above.
