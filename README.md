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
