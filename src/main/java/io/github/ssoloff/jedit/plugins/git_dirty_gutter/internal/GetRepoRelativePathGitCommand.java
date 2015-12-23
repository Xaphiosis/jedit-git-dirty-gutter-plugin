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

/**
 * A Git command that obtains the repository-relative path of a file at the HEAD
 * revision.
 */
final class GetRepoRelativePathGitCommand extends GitCommand implements LineVisitor {
    private String repoRelativePath;

    /**
     * Initializes a new instance of the {@code GetRepoRelativePathGitCommand}
     * class using the specified path.
     *
     * @param path
     *        The absolute path of the file whose repository-relative path is
     *        desired; must not be {@code null}.
     */
    GetRepoRelativePathGitCommand(final String path) {
        super(path);

        assert path != null;

        // TODO
    }

    /**
     * Initializes a new instance of the {@code GetRepoRelativePathGitCommand}
     * class using the specified process executor.
     *
     * @param executor
     *        The process executor; must not be {@code null}.
     */
    GetRepoRelativePathGitCommand(final ProcessExecutor executor) {
        super(executor);

        assert executor != null;
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
    String getRepoRelativePath() {
        return repoRelativePath;
    }

    /*
     * @see common.io.ProcessExecutor.LineVisitor#process(java.lang.String, boolean)
     */
    @Override
    public boolean process(final String line, final boolean isError) {
        if (isError) {
            return true;
        }

        repoRelativePath = line;
        return false;
    }
}
