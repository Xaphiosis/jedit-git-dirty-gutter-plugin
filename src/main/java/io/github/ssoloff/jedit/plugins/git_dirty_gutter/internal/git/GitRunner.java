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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.git;

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.IProcessRunner;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Implementation of {@link IGitRunner}.
 */
public final class GitRunner implements IGitRunner {
    private final IProcessRunner processRunner;
    private final Path programPath;
    private final Path workingDirPath;

    /**
     * Initializes a new instance of the {@code GitRunner} class.
     *
     * @param processRunner
     *        The process runner used to run Git.
     * @param programPath
     *        The program path of the Git process to run.
     * @param workingDirPath
     *        The working directory path of the Git process to run, typically a
     *        directory within the Git repository that is the target of the
     *        command.
     */
    public GitRunner(final IProcessRunner processRunner, final Path workingDirPath, final Path programPath) {
        this.processRunner = processRunner;
        this.programPath = programPath;
        this.workingDirPath = workingDirPath;
    }

    private String[] createCommand(final String[] args) {
        final String[] command = new String[args.length + 1];
        command[0] = programPath.toString();
        System.arraycopy(args, 0, command, 1, args.length);
        return command;
    }

    private GitException createGitExitedWithErrorException(final String[] args, final int exitCode,
            final String error) {
        return GitException.newBuilder() //
                .withMessageSummary("the Git process exited with an error") //$NON-NLS-1$
                .withWorkingDirPath(workingDirPath) //
                .withProgramPath(programPath) //
                .withProgramArgs(args) //
                .withExitCode(exitCode) //
                .withError(error) //
                .build();
    }

    @Override
    public Path getProgramPath() {
        return programPath;
    }

    @Override
    public Path getWorkingDirPath() {
        return workingDirPath;
    }

    @Override
    public int run(final Writer outWriter, final String... args)
            throws GitException, IOException, InterruptedException {
        final StringWriter errWriter = new StringWriter();
        final int exitCode = processRunner.run(outWriter, errWriter, workingDirPath, createCommand(args));
        final String error = errWriter.toString();
        if (!error.isEmpty()) {
            throw createGitExitedWithErrorException(args, exitCode, error);
        }
        return exitCode;
    }
}
