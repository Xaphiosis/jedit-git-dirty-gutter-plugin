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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model;

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.StringUtils;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitException;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitRunnerResult;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunner;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A facade for running various custom Git commands required by the model.
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

    private static GitException createUnexpectedGitExitCodeException(final GitRunnerResult result) {
        return newGitExceptionBuilder(result) //
                .withMessageSummary("unexpected Git exit code") //$NON-NLS-1$
                .withExitCode(result.getExitCode()) //
                .build();
    }

    private static GitException createUnexpectedGitOutputException(final GitRunnerResult result,
            final List<String> lines) {
        return newGitExceptionBuilder(result) //
                .withMessageSummary("unexpected Git output") //$NON-NLS-1$
                .withOutput(StringUtils.joinLinesWithImplicitFinalLine(lines)) //
                .build();
    }

    /**
     * Gets the SHA-1 commit reference for the specified file at the HEAD
     * revision.
     *
     * @param repoRelativeFilePath
     *        The repository-relative path of the file whose commit reference is
     *        desired.
     *
     * @return The SHA-1 commit reference for the specified file at the HEAD
     *         revision.
     *
     * @throws GitException
     *         If the Git process exits with an error.
     * @throws IOException
     *         If an error occurs while processing the Git process output.
     * @throws InterruptedException
     *         If interrupted while waiting for the Git process to exit.
     */
    String getCommitRefAtHeadRevision(final Path repoRelativeFilePath)
            throws GitException, IOException, InterruptedException {
        final StringWriter outWriter = new StringWriter();
        final String[] programArgs = new String[] {
            "rev-parse", //$NON-NLS-1$
            String.format("HEAD:%s", repoRelativeFilePath) //$NON-NLS-1$
        };
        final GitRunnerResult result = gitRunner.run(outWriter, programArgs);
        if (result.getExitCode() != 0) {
            throw createUnexpectedGitExitCodeException(result);
        }

        final List<String> lines = StringUtils.splitLinesWithImplicitFinalLine(outWriter.getBuffer());
        if (lines.size() != 1) {
            throw createUnexpectedGitOutputException(result, lines);
        }

        final String commitRef = lines.get(0);
        assert commitRef != null;
        return commitRef;
    }

    /**
     * Gets the repository-relative path of the specified file at the HEAD
     * revision.
     *
     * @param filePath
     *        The path to the file whose repository-relative path is desired.
     *
     * @return The repository-relative path of the specified file at the HEAD
     *         revision.
     *
     * @throws GitException
     *         If the Git process exits with an error.
     * @throws IOException
     *         If an error occurs while processing the Git process output.
     * @throws InterruptedException
     *         If interrupted while waiting for the Git process to exit.
     */
    Path getRepoRelativeFilePathAtHeadRevision(final Path filePath)
            throws GitException, IOException, InterruptedException {
        final StringWriter outWriter = new StringWriter();
        final String[] programArgs = new String[] {
            "ls-tree", //$NON-NLS-1$
            "--full-name", //$NON-NLS-1$
            "--name-only", //$NON-NLS-1$
            "HEAD", //$NON-NLS-1$
            filePath.toString() //
        };
        final GitRunnerResult result = gitRunner.run(outWriter, programArgs);
        if (result.getExitCode() != 0) {
            throw createUnexpectedGitExitCodeException(result);
        }

        final List<String> lines = StringUtils.splitLinesWithImplicitFinalLine(outWriter.getBuffer());
        if (lines.size() != 1) {
            throw createUnexpectedGitOutputException(result, lines);
        }

        return Paths.get(lines.get(0));
    }

    /**
     * Indicates the specified file exists at the HEAD revision.
     *
     * @param filePath
     *        The path to the file whose existence at the HEAD revision is to be
     *        determined.
     *
     * @return {@code true} if the specified file exists at the HEAD revision;
     *         otherwise {@code false}.
     *
     * @throws GitException
     *         If the Git process exits with an unexpected error.
     * @throws IOException
     *         If an error occurs while processing the Git process output.
     * @throws InterruptedException
     *         If interrupted while waiting for the Git process to exit.
     */
    boolean isFilePresentAtHeadRevision(final Path filePath) throws GitException, IOException, InterruptedException {
        final StringWriter outWriter = new StringWriter();
        final String[] programArgs = new String[] {
            "ls-tree", //$NON-NLS-1$
            "--full-name", //$NON-NLS-1$
            "--name-only", //$NON-NLS-1$
            "HEAD", //$NON-NLS-1$
            filePath.toString() //
        };
        try {
            final GitRunnerResult result = gitRunner.run(outWriter, programArgs);
            if (result.getExitCode() != 0) {
                return false;
            }
        } catch (final GitException e) {
            final Integer gitFatalExitCode = Integer.valueOf(128);
            final Integer exitCode = e.getExitCode();
            if ((exitCode != null) && (exitCode == gitFatalExitCode)) {
                return false;
            }
            throw e;
        }

        final List<String> lines = StringUtils.splitLinesWithImplicitFinalLine(outWriter.getBuffer());
        if (lines.size() != 1) {
            return false;
        }

        return true;
    }

    private static GitException.Builder newGitExceptionBuilder(final GitRunnerResult result) {
        return GitException.newBuilder() //
                .withWorkingDirPath(result.getWorkingDirPath()) //
                .withCommand(result.getCommand());
    }

    /**
     * Reads the content of the specified file at the HEAD revision and sends it
     * to the specified writer.
     *
     * @param repoRelativeFilePath
     *        The repository-relative path of the file whose content is to be
     *        read.
     * @param writer
     *        The writer that will receive the file content.
     *
     * @throws GitException
     *         If the Git process exits with an error.
     * @throws IOException
     *         If an error occurs while processing the Git process output.
     * @throws InterruptedException
     *         If interrupted while waiting for the Git process to exit.
     */
    void readFileContentAtHeadRevision(final Path repoRelativeFilePath, final Writer writer)
            throws GitException, IOException, InterruptedException {
        final String[] programArgs = new String[] {
            "show", //$NON-NLS-1$
            String.format("HEAD:%s", repoRelativeFilePath) //$NON-NLS-1$
        };
        final GitRunnerResult result = gitRunner.run(writer, programArgs);
        if (result.getExitCode() != 0) {
            throw createUnexpectedGitExitCodeException(result);
        }
    }
}
