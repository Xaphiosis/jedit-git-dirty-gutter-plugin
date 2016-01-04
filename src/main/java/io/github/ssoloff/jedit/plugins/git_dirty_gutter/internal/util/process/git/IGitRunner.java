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

import java.io.IOException;
import java.io.Writer;

/**
 * A Git process runner.
 */
public interface IGitRunner {
    /**
     * Runs a new Git process.
     *
     * @param outWriter
     *        The writer that will receive the content of the standard output
     *        stream of the Git process.
     * @param programArgs
     *        The arguments to pass to the Git process.
     *
     * @return The result of running the Git process.
     *
     * @throws GitException
     *         If the Git process exits with an error.
     * @throws IOException
     *         If an error occurs while running the Git process.
     * @throws InterruptedException
     *         If interrupted while waiting for the Git process to exit.
     */
    GitRunnerResult run(Writer outWriter, String... programArgs) throws GitException, IOException, InterruptedException;
}
