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

import java.util.Arrays;

/**
 * A checked exception that indicates a Git process exited with an error.
 */
final class GitException extends Exception {
    private static final long serialVersionUID = -4617699499893940183L;

    private final String[] command;
    private final String errorMessage;
    private final int exitCode;

    /**
     * Initializes a new instance of the {@code GitException} class.
     *
     * @param command
     *        The command that was run by the Git process.
     * @param exitCode
     *        The exit code of the Git process.
     * @param errorMessage
     *        The error message, typically the content of the standard error
     *        stream of the Git process.
     */
    GitException(final String[] command, final int exitCode, final String errorMessage) {
        this.command = cloneArray(command);
        this.errorMessage = errorMessage;
        this.exitCode = exitCode;
    }

    @SuppressWarnings("null")
    private static <T> T[] cloneArray(final T[] array) {
        return array.clone();
    }

    /**
     * Gets the command that was run by the Git process.
     *
     * @return The command that was run by the Git process.
     */
    String[] getCommand() {
        return cloneArray(command);
    }

    /**
     * Gets the error message.
     *
     * @return The error message.
     */
    String getErrorMessage() {
        return errorMessage;
    }

    /**
     * The exit code of the Git process.
     *
     * @return The exit code of the Git process.
     */
    int getExitCode() {
        return exitCode;
    }

    @Override
    @SuppressWarnings("boxing")
    public String getMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("Git command: %s\n", Arrays.toString(command))); //$NON-NLS-1$
        sb.append(String.format("  exit code: %d\n", exitCode)); //$NON-NLS-1$
        sb.append(String.format("      error: %s", errorMessage)); //$NON-NLS-1$
        return sb.toString();
    }
}
