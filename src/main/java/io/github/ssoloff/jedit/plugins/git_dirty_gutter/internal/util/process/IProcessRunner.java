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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 * A process runner.
 */
public interface IProcessRunner {
    /**
     * Runs a new process.
     *
     * @param outWriter
     *        The writer that will receive the content of the standard output
     *        stream of the process.
     * @param errWriter
     *        The writer that will receive the content of the standard error
     *        stream of the process.
     * @param workingDirPath
     *        The path to the process working directory.
     * @param command
     *        The command to run. The first element is the process executable.
     *        The remaining elements are the process arguments.
     *
     * @return The exit code of the process.
     *
     * @throws IOException
     *         If an error occurs while running the process.
     * @throws InterruptedException
     *         If interrupted while waiting for the process to exit.
     */
    public int run(Writer outWriter, Writer errWriter, Path workingDirPath, String... command)
            throws IOException, InterruptedException;
}
