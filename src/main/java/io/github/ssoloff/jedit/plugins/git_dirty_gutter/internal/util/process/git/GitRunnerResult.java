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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git;

import java.nio.file.Path;

/**
 * The result of running a Git process using the {@link IGitRunner#run} method.
 */
public final class GitRunnerResult {
    private final int exitCode;
    private final Path programPath;
    private final Path workingDirPath;

    /**
     * Initializes a new instance of the {@code GitRunnerResult} class.
     *
     * @param workingDirPath
     *        The working directory path of the Git process that was executed.
     * @param programPath
     *        The program path of the Git process that was executed.
     * @param exitCode
     *        The exit code of the Git process that was executed.
     */
    public GitRunnerResult(final Path workingDirPath, final Path programPath, final int exitCode) {
        this.exitCode = exitCode;
        this.programPath = programPath;
        this.workingDirPath = workingDirPath;
    }

    /**
     * Gets the exit code of the Git process that was executed.
     *
     * @return The exit code of the Git process that was executed.
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Gets the program path of the Git process that was executed.
     *
     * @return The program path of the Git process that was executed.
     */
    public Path getProgramPath() {
        return programPath;
    }

    /**
     * Gets the working directory path of the Git process that was executed.
     *
     * @return The working directory path of the Git process that was executed.
     */
    public Path getWorkingDirPath() {
        return workingDirPath;
    }
}
