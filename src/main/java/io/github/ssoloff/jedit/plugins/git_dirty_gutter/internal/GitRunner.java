/*
 * Copyright (C) 2015 Steven Soloff
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Implementation of {@link IGitRunner}.
 */
final class GitRunner implements IGitRunner {
    private final Path gitPath;
    private final IProcessRunner processRunner;
    private final Path workingDirPath;

    /**
     * Initializes a new instance of the {@code GitRunner} class.
     *
     * @param processRunner
     *        The process runner used to run Git.
     * @param gitPath
     *        The path to the Git executable.
     * @param workingDirPath
     *        The path to the process working directory, typically a directory
     *        within the Git repository that is the target of the command.
     */
    GitRunner(final IProcessRunner processRunner, final Path gitPath, final Path workingDirPath) {
        this.gitPath = gitPath;
        this.processRunner = processRunner;
        this.workingDirPath = workingDirPath;
    }

    private String[] createCommand(final String[] args) {
        final String[] command = new String[args.length + 1];
        command[0] = gitPath.toString();
        System.arraycopy(args, 0, command, 1, args.length);
        return command;
    }

    /*
     * @see io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.IGitRunner#run(java.io.Writer, java.lang.String[])
     */
    @Override
    public void run(final Writer outWriter, final String... args)
            throws GitException, IOException, InterruptedException {
        final StringWriter errWriter = new StringWriter();
        final int exitCode = processRunner.run(outWriter, errWriter, workingDirPath, createCommand(args));
        if (exitCode != 0) {
            throw new GitException(exitCode, errWriter.toString());
        }
    }
}
