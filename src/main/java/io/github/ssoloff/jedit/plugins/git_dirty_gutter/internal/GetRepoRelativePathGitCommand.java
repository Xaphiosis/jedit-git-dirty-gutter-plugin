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

import common.io.ProcessExecutor;
import common.io.ProcessExecutor.LineVisitor;
import git.GitCommand;
import git.GitPlugin;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A Git command that obtains the repository-relative path of a file at the HEAD
 * revision.
 */
final class GetRepoRelativePathGitCommand extends GitCommand implements LineVisitor {
    private @Nullable Path repoRelativePath = null;

    /**
     * Initializes a new instance of the {@code GetRepoRelativePathGitCommand}
     * class using the default Git path.
     *
     * @param filePath
     *        The path to the file whose repository-relative path is desired.
     */
    GetRepoRelativePathGitCommand(final Path filePath) {
        this(filePath, Paths.get(GitPlugin.gitPath()));
    }

    /**
     * Initializes a new instance of the {@code GetRepoRelativePathGitCommand}
     * class using the specified Git path.
     *
     * @param filePath
     *        The path to the file whose repository-relative path is desired.
     * @param gitPath
     *        The path to the Git executable.
     */
    GetRepoRelativePathGitCommand(final Path filePath, final Path gitPath) {
        super(createExecutor(gitPath, filePath));

        getExecutor().addVisitor(this);
    }

    private static ProcessExecutor createExecutor(final Path gitPath, final Path filePath) {
        final ProcessExecutor executor = new ProcessExecutor( //
                gitPath.toString(), //
                "ls-tree", //$NON-NLS-1$
                "--full-name", //$NON-NLS-1$
                "--name-only", //$NON-NLS-1$
                "HEAD", //$NON-NLS-1$
                filePath.toString() //
        );
        executor.setDirectory(filePath.getParent().toString());
        return executor;
    }

    /**
     * Gets the repository-relative path of the file associated with the command
     * at the HEAD revision.
     *
     * @return The repository-relative path of the file associated with the
     *         command at the HEAD revision or {@code null} if the file does not
     *         exist in the repository at the HEAD revision or if an error
     *         occurred while executing the command.
     */
    @Nullable
    Path getRepoRelativePath() {
        return repoRelativePath;
    }

    @Override
    public boolean process(final String line, final boolean isError) {
        if (isError) {
            return true;
        }

        repoRelativePath = Paths.get(line);
        return false;
    }
}
