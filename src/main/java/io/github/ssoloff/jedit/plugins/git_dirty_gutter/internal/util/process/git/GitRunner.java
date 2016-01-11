/*
 * Copyright (C) 2015-2016 Steven Soloff
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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git;

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.ISupplier;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.Suppliers;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.IProcessRunner;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link IGitRunner}.
 */
public final class GitRunner implements IGitRunner {
    private final IProcessRunner processRunner;
    private final ISupplier<Path> programPathSupplier;
    private final Path workingDirPath;

    /**
     * Initializes a new instance of the {@code GitRunner} class using the
     * specified program path.
     *
     * @param processRunner
     *        The process runner used to run Git.
     * @param workingDirPath
     *        The working directory path of the Git process to run, typically a
     *        directory within the Git repository that is the target of the
     *        command.
     * @param programPath
     *        The program path of the Git process to run.
     */
    public GitRunner(final IProcessRunner processRunner, final Path workingDirPath, final Path programPath) {
        this(processRunner, workingDirPath, Suppliers.forObject(programPath));
    }

    /**
     * Initializes a new instance of the {@code GitRunner} class using the
     * specified program path supplier.
     *
     * @param processRunner
     *        The process runner used to run Git.
     * @param workingDirPath
     *        The working directory path of the Git process to run, typically a
     *        directory within the Git repository that is the target of the
     *        command.
     * @param programPathSupplier
     *        The supplier of the program path of the Git process to run.
     */
    public GitRunner(final IProcessRunner processRunner, final Path workingDirPath,
            final ISupplier<Path> programPathSupplier) {
        this.processRunner = processRunner;
        this.programPathSupplier = programPathSupplier;
        this.workingDirPath = workingDirPath;
    }

    private static List<String> createCommand(final Path programPath, final List<String> programArgs) {
        final List<String> command = new ArrayList<>(programArgs.size() + 1);
        command.add(programPath.toString());
        command.addAll(programArgs);
        return command;
    }

    private static GitException createGitExitedWithErrorException(final GitRunnerResult result, final String error) {
        return GitException.newBuilder() //
                .withMessageSummary("the Git process exited with an error") //$NON-NLS-1$
                .withWorkingDirPath(result.getWorkingDirPath()) //
                .withCommand(result.getCommand()) //
                .withExitCode(result.getExitCode()) //
                .withError(error) //
                .build();
    }

    @Override
    public GitRunnerResult run(final Writer outWriter, final String... programArgs)
            throws GitException, IOException, InterruptedException {
        final StringWriter errWriter = new StringWriter();
        final List<String> command = createCommand(programPathSupplier.get(), Arrays.asList(programArgs));
        final int exitCode = processRunner.run(outWriter, errWriter, workingDirPath,
                command.toArray(new String[command.size()]));
        final GitRunnerResult result = new GitRunnerResult(workingDirPath, command, exitCode);

        final String error = errWriter.toString();
        if (!error.isEmpty()) {
            throw createGitExitedWithErrorException(result, error);
        }

        return result;
    }
}
