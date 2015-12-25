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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A facade for running various custom Git commands required by the plugin.
 */
final class GitCommands {
    private final IGitRunner gitRunner;

    /**
     * Initializes a new instance of the {@code GitCommands} class.
     *
     * @param gitRunner
     *        The Git process runner.
     */
    GitCommands(final IGitRunner gitRunner) {
        this.gitRunner = gitRunner;
    }

    /**
     * Gets the repository-relative path at the HEAD revision of the specified
     * file.
     *
     * @param filePath
     *        The path to the file whose repository-relative path is desired.
     * 
     * @return The repository-relative path at the HEAD revision of the
     *         specified file or {@code null} if the file does not exist in the
     *         repository at the HEAD revision or if an error occurred while
     *         executing the command.
     * 
     * @throws GitRunnerException
     *         If the Git process exits with an error.
     * @throws IOException
     *         If an error occurs while processing the Git process output.
     * @throws InterruptedException
     *         If interrupted while waiting for the Git process to exit.
     */
    @Nullable
    Path getRepoRelativePath(final Path filePath) throws GitRunnerException, IOException, InterruptedException {
        final StringWriter outWriter = new StringWriter();
        final String[] args = new String[] {
            "ls-tree", //$NON-NLS-1$
            "--full-name", //$NON-NLS-1$
            "--name-only", //$NON-NLS-1$
            "HEAD", //$NON-NLS-1$
            filePath.toString() //
        };
        gitRunner.run(outWriter, args);
        final List<String> lines = readAllLines(outWriter);
        return (lines.size() == 1) ? Paths.get(lines.get(0)) : null;
    }

    private static List<String> readAllLines(final StringWriter writer) throws IOException {
        final List<String> lines = new ArrayList<>();
        try (final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
