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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A checked exception that indicates a Git process exited with an error or that
 * the Git process produced an unexpected result (exit code, output, etc.).
 */
public final class GitException extends Exception {
    private static final long serialVersionUID = -4617699499893940183L;

    @Nullable
    private final List<String> command;

    @Nullable
    private final String error;

    @Nullable
    private final Integer exitCode;

    @Nullable
    private final String messageSummary;

    @Nullable
    private final String output;

    @Nullable
    private final Path workingDirPath;

    /**
     * Initializes a new instance of the {@code GitException} class.
     *
     * @param messageSummary
     *        The exception message summary or {@code null} if not specified.
     * @param workingDirPath
     *        The working directory path of the Git process or {@code null} if
     *        not specified.
     * @param command
     *        The command line of the Git process or {@code null} if not
     *        specified.
     * @param exitCode
     *        The exit code of the Git process or {@code null} if not specified.
     * @param output
     *        The content of the standard output stream of the Git process or
     *        {@code null} if not specified.
     * @param error
     *        The content of the standard error stream of the Git process or
     *        {@code null} if not specified.
     */
    GitException(@Nullable final String messageSummary, @Nullable final Path workingDirPath,
            @Nullable final List<String> command, @Nullable final Integer exitCode, @Nullable final String output,
            @Nullable final String error) {
        this.command = (command != null) ? new ArrayList<>(command) : null;
        this.error = error;
        this.exitCode = exitCode;
        this.messageSummary = messageSummary;
        this.output = output;
        this.workingDirPath = workingDirPath;
    }

    /**
     * Gets the command line of the Git process.
     *
     * @return The command line of the Git process or {@code null} if not
     *         specified.
     */
    @Nullable
    public List<String> getCommand() {
        return (command != null) ? new ArrayList<>(command) : null;
    }

    /**
     * Gets the default exception message summary.
     *
     * @return The default exception message summary.
     */
    static String getDefaultMessageSummary() {
        return "an unspecified Git error has occurred"; //$NON-NLS-1$
    }

    /**
     * Gets the content of the standard error stream of the Git process.
     *
     * @return The content of the standard error stream of the Git process or
     *         {@code null} if not specified.
     */
    @Nullable
    public String getError() {
        return error;
    }

    /**
     * The exit code of the Git process.
     *
     * @return The exit code of the Git process.
     */
    @Nullable
    public Integer getExitCode() {
        return exitCode;
    }

    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder();

        if (isPopulated(messageSummary)) {
            sb.append(String.format("%s%n", messageSummary)); //$NON-NLS-1$
        } else {
            sb.append(String.format("%s%n", getDefaultMessageSummary())); //$NON-NLS-1$
        }

        if (isPopulated(workingDirPath)) {
            sb.append(String.format("working dir: %s%n", workingDirPath)); //$NON-NLS-1$
        }

        if (isPopulated(command)) {
            sb.append(String.format("    command: %s%n", command)); //$NON-NLS-1$
        }

        if (isPopulated(exitCode)) {
            sb.append(String.format("  exit code: %d%n", exitCode)); //$NON-NLS-1$
        }

        if (isPopulated(output)) {
            sb.append(String.format("     output: %s%n", output)); //$NON-NLS-1$
        }

        if (isPopulated(error)) {
            sb.append(String.format("      error: %s", error)); //$NON-NLS-1$
        }

        return sb.toString();
    }

    /**
     * Gets the content of the standard output stream of the Git process.
     *
     * @return The content of the standard output stream of the Git process or
     *         {@code null} if not specified.
     */
    @Nullable
    public String getOutput() {
        return output;
    }

    /**
     * Gets the working directory path of the Git process.
     *
     * @return The working directory path of the Git process or {@code null} if
     *         not specified.
     */
    @Nullable
    public Path getWorkingDirPath() {
        return workingDirPath;
    }

    private static boolean isPopulated(@Nullable final Object obj) {
        return obj != null;
    }

    private static boolean isPopulated(@Nullable final String str) {
        return (str != null) && !str.isEmpty();
    }

    /**
     * Creates a new builder for creating instances of the {@code GitException}
     * class.
     *
     * @return A new builder for creating instances of the {@code GitException}
     *         class.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for creating instances of the {@code GitException} class.
     */
    public static final class Builder {
        @Nullable
        private List<String> command = null;

        @Nullable
        private String error = null;

        @Nullable
        private Integer exitCode = null;

        @Nullable
        private String messageSummary = null;

        @Nullable
        private String output = null;

        @Nullable
        private Path workingDirPath = null;

        /**
         * Initializes a new instance of the {@code Builder} class.
         */
        Builder() {
            // do nothing
        }

        /**
         * Creates a new exception based on the state of the builder.
         *
         * @return A new exception based on the state of the builder.
         */
        public GitException build() {
            return new GitException(messageSummary, workingDirPath, command, exitCode, output, error);
        }

        /**
         * Sets the command line of the Git process.
         *
         * @param command
         *        The command line of the Git process.
         *
         * @return The builder.
         */
        public Builder withCommand(
                @SuppressWarnings({ "checkstyle:hiddenfield", "hiding" }) final List<String> command) {
            this.command = command;
            return this;
        }

        /**
         * Sets the content of the standard error stream of the Git process.
         *
         * @param error
         *        The content of the standard error stream of the Git process.
         *
         * @return The builder.
         */
        public Builder withError(@SuppressWarnings({ "checkstyle:hiddenfield", "hiding" }) final String error) {
            this.error = error;
            return this;
        }

        /**
         * Sets the exit code of the Git process.
         *
         * @param exitCode
         *        The exit code of the Git process.
         *
         * @return The builder.
         */
        public Builder withExitCode(@SuppressWarnings({ "checkstyle:hiddenfield", "hiding" }) final int exitCode) {
            this.exitCode = Integer.valueOf(exitCode);
            return this;
        }

        /**
         * Sets the exception message summary.
         *
         * @param messageSummary
         *        The exception message summary.
         *
         * @return The builder.
         */
        public Builder withMessageSummary(
                @SuppressWarnings({ "checkstyle:hiddenfield", "hiding" }) final String messageSummary) {
            this.messageSummary = messageSummary;
            return this;
        }

        /**
         * Sets the content of the standard output stream of the Git process.
         *
         * @param output
         *        The content of the standard output stream of the Git process.
         *
         * @return The builder.
         */
        public Builder withOutput(@SuppressWarnings({ "checkstyle:hiddenfield", "hiding" }) final String output) {
            this.output = output;
            return this;
        }

        /**
         * Sets the working directory path of the Git process.
         *
         * @param workingDirPath
         *        The working directory path of the Git process.
         *
         * @return The builder.
         */
        public Builder withWorkingDirPath(
                @SuppressWarnings({ "checkstyle:hiddenfield", "hiding" }) final Path workingDirPath) {
            this.workingDirPath = workingDirPath;
            return this;
        }
    }
}
