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

import java.nio.file.Path;
import java.util.Arrays;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A checked exception that indicates a Git process exited with an error or that
 * the Git process produced an unexpected result (exit code, output, etc.).
 */
final class GitException extends Exception {
    private static final long serialVersionUID = -4617699499893940183L;

    private final @Nullable String error;
    private final @Nullable Integer exitCode;
    private final @Nullable String messageSummary;
    private final @Nullable String output;
    private final @Nullable String[] programArgs;
    private final @Nullable Path programPath;

    /**
     * Initializes a new instance of the {@code GitException} class.
     *
     * @param messageSummary
     *        The exception message summary or {@code null} if not specified.
     * @param programPath
     *        The program path of the Git process or {@code null} if not
     *        specified.
     * @param programArgs
     *        The program arguments to the Git process or {@code null} if not
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
    private GitException(final @Nullable String messageSummary, final @Nullable Path programPath,
            final @Nullable String[] programArgs, final @Nullable Integer exitCode, final @Nullable String output,
            final @Nullable String error) {
        this.error = error;
        this.exitCode = exitCode;
        this.messageSummary = messageSummary;
        this.output = output;
        this.programArgs = programArgs; // defensive copy already performed by builder
        this.programPath = programPath;
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
    String getError() {
        return error;
    }

    /**
     * The exit code of the Git process.
     *
     * @return The exit code of the Git process.
     */
    @Nullable
    Integer getExitCode() {
        return exitCode;
    }

    /**
     * Gets the content of the standard output stream of the Git process.
     *
     * @return The content of the standard output stream of the Git process or
     *         {@code null} if not specified.
     */
    @Nullable
    String getOutput() {
        return output;
    }

    /**
     * Gets the program arguments to the Git process.
     *
     * @return The program arguments to the Git process or {@code null} if not
     *         specified.
     */
    @Nullable
    String[] getProgramArgs() {
        return (programArgs != null) ? programArgs.clone() : null;
    }

    /**
     * Gets the program path of the Git process.
     *
     * @return The program path of the Git process or {@code null} if not
     *         specified.
     */
    @Nullable
    Path getProgramPath() {
        return programPath;
    }

    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder();

        if (isPopulated(messageSummary)) {
            sb.append(String.format("%s\n", messageSummary)); //$NON-NLS-1$
        } else {
            sb.append(String.format("%s\n", getDefaultMessageSummary())); //$NON-NLS-1$
        }

        if (isPopulated(programPath)) {
            sb.append(String.format("program path: %s\n", programPath)); //$NON-NLS-1$
        }

        if (isPopulated(programArgs)) {
            sb.append(String.format("program args: %s\n", Arrays.toString(programArgs))); //$NON-NLS-1$
        }

        if (isPopulated(exitCode)) {
            sb.append(String.format("   exit code: %d\n", exitCode)); //$NON-NLS-1$
        }

        if (isPopulated(output)) {
            sb.append(String.format("      output: %s\n", output)); //$NON-NLS-1$
        }

        if (isPopulated(error)) {
            sb.append(String.format("       error: %s", error)); //$NON-NLS-1$
        }

        return sb.toString();
    }

    private static boolean isPopulated(final @Nullable Object obj) {
        return (obj != null);
    }

    private static boolean isPopulated(final @Nullable String str) {
        return (str != null) && !str.isEmpty();
    }

    /**
     * Creates a new builder for creating instances of the {@code GitException}
     * class.
     *
     * @return A new builder for creating instances of the {@code GitException}
     *         class.
     */
    @SuppressWarnings("synthetic-access")
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for creating instances of the {@code GitException} class.
     */
    static final class Builder {
        private @Nullable String error = null;
        private @Nullable Integer exitCode = null;
        private @Nullable String messageSummary = null;
        private @Nullable String output = null;
        private @Nullable String[] programArgs = null;
        private @Nullable Path programPath = null;

        private Builder() {
        }

        /**
         * Creates a new exception based on the state of the builder.
         *
         * @return A new exception based on the state of the builder.
         */
        @SuppressWarnings("synthetic-access")
        GitException build() {
            return new GitException(messageSummary, programPath, programArgs, exitCode, output, error);
        }

        /**
         * Sets the content of the standard error stream of the Git process.
         *
         * @param error
         *        The content of the standard error stream of the Git process.
         *
         * @return The builder.
         */
        Builder withError(@SuppressWarnings("hiding") final String error) {
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
        Builder withExitCode(@SuppressWarnings("hiding") final int exitCode) {
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
        Builder withMessageSummary(@SuppressWarnings("hiding") final String messageSummary) {
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
        Builder withOutput(@SuppressWarnings("hiding") final String output) {
            this.output = output;
            return this;
        }

        /**
         * Sets the program arguments to the Git process.
         *
         * @param programArgs
         *        The program arguments to the Git process.
         *
         * @return The builder.
         */
        Builder withProgramArgs(@SuppressWarnings("hiding") final String[] programArgs) {
            this.programArgs = programArgs.clone();
            return this;
        }

        /**
         * Sets the program path of the Git process.
         *
         * @param programPath
         *        The program path of the Git process.
         *
         * @return The builder.
         */
        Builder withProgramPath(@SuppressWarnings("hiding") final Path programPath) {
            this.programPath = programPath;
            return this;
        }
    }
}
