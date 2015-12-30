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

import difflib.DiffUtils;
import difflib.Patch;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.IBuffer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.ILog;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.StringUtils;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A facade for running various custom high-level tasks required by the plugin.
 */
public final class GitTasks {
    private final IGitRunnerFactory gitRunnerFactory;
    private final ILog log;

    /**
     * Initializes a new instance of the {@code GitTasks} class.
     *
     * @param gitRunnerFactory
     *        The factory used to create Git runners.
     * @param log
     *        The application log.
     */
    public GitTasks(final IGitRunnerFactory gitRunnerFactory, final ILog log) {
        this.gitRunnerFactory = gitRunnerFactory;
        this.log = log;
    }

    private GitCommands createGitCommands(final IBuffer buffer) throws IOException {
        final Path filePath = buffer.getFilePath();
        final Path workingDirPath = filePath.getParent();
        if (workingDirPath == null) {
            throw new IOException(String.format("unable to get directory for '%s'", filePath)); //$NON-NLS-1$
        }
        final IGitRunner gitRunner = gitRunnerFactory.createGitRunner(workingDirPath);
        return new GitCommands(gitRunner);
    }

    /**
     * Creates a patch between the HEAD revision of the file associated with the
     * specified buffer and the current state of the buffer.
     *
     * @param buffer
     *        The buffer for which the patch is to be created.
     *
     * @return The patch between the HEAD revision of the file associated with
     *         the specified buffer and the current state of the buffer or
     *         {@code null} if the patch cannot be created.
     *
     * @throws InterruptedException
     *         If interrupted while waiting for the task to complete.
     */
    public @Nullable Patch createPatchBetweenHeadRevisionAndCurrentState(final IBuffer buffer)
            throws InterruptedException {
        if (!isFilePresentAtHeadRevision(buffer)) {
            return null;
        }

        try {
            final List<String> headRevisionLines = getHeadRevisionLines(buffer);
            return DiffUtils.diff(headRevisionLines, buffer.getLines());
        } catch (final GitException | IOException e) {
            log.logError(this,
                    String.format("failed to create patch between HEAD revision of file and current state (%s)", //$NON-NLS-1$
                            buffer.getFilePath()),
                    e);
            return null;
        }
    }

    private String getCommitRefAtHeadRevision(final IBuffer buffer)
            throws GitException, IOException, InterruptedException {
        final GitCommands gitCommands = createGitCommands(buffer);
        final Path repoRelativeFilePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(buffer.getFilePath());
        return gitCommands.getCommitRefAtHeadRevision(repoRelativeFilePath);
    }

    private List<String> getHeadRevisionLines(final IBuffer buffer)
            throws GitException, IOException, InterruptedException {
        final GitCommands gitCommands = createGitCommands(buffer);
        final Path repoRelativeFilePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(buffer.getFilePath());
        final StringWriter headRevisionFileWriter = new StringWriter();
        gitCommands.readFileContentAtHeadRevision(repoRelativeFilePath, headRevisionFileWriter);
        return StringUtils.splitLinesWithExplicitFinalLine(headRevisionFileWriter.getBuffer());
    }

    /**
     * Indicates the HEAD revision commit reference of the file associated with
     * the specified buffer differs from the specified commit reference.
     *
     * @param buffer
     *        The buffer whose associated file will have its HEAD revision
     *        commit reference compared to the specified commit reference.
     * @param commitRefRef
     *        On input, specifies the commit reference to which the HEAD
     *        revision commit reference is to be compared. On output, receives
     *        the current HEAD revision commit reference.
     *
     * @return {@code true} if the HEAD revision commit reference of the file
     *         associated with the specified buffer differs from the specified
     *         commit reference; otherwise {@code false}.
     *
     * @throws InterruptedException
     *         If interrupted while waiting for the task to complete.
     */
    public boolean hasHeadRevisionChanged(final IBuffer buffer, final AtomicReference<String> commitRefRef)
            throws InterruptedException {
        if (!isFilePresentAtHeadRevision(buffer)) {
            return false;
        }

        try {
            final String previousCommitRef = commitRefRef.get();
            final String currentCommitRef = getCommitRefAtHeadRevision(buffer);
            if (!Objects.equals(previousCommitRef, currentCommitRef)) {
                commitRefRef.set(currentCommitRef);
                return true;
            }
        } catch (final GitException | IOException e) {
            log.logError(this, String.format("failed to determine if HEAD revision of file has changed (%s)", //$NON-NLS-1$
                    buffer.getFilePath()), e);
        }

        return false;
    }

    private boolean isFilePresentAtHeadRevision(final IBuffer buffer) throws InterruptedException {
        try {
            final GitCommands gitCommands = createGitCommands(buffer);
            if (!gitCommands.isFilePresentAtHeadRevision(buffer.getFilePath())) {
                log.logDebug(this, String.format("file not present at HEAD revision (%s)", buffer.getFilePath())); //$NON-NLS-1$
                return false;
            }
        } catch (final GitException | IOException e) {
            log.logError(this,
                    String.format("failed to determine if file present at HEAD revision (%s)", buffer.getFilePath()), //$NON-NLS-1$
                    e);
            return false;
        }

        return true;
    }
}