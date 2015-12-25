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

/**
 * A checked exception that indicates the Git process run by an instance of
 * {@link IGitRunner} exited with an error.
 */
final class GitRunnerException extends Exception {
    private static final long serialVersionUID = -4617699499893940183L;

    private final int exitCode;

    /**
     * Initializes a new instance of the {@code GitRunnerException} class.
     *
     * @param exitCode
     *        The exit code of the Git process.
     * @param message
     *        The detail message, typically the standard output of the Git
     *        process.
     */
    GitRunnerException(final int exitCode, final String message) {
        super(message);

        this.exitCode = exitCode;
    }

    /**
     * The exit code of the Git process.
     *
     * @return The exit code of the Git process.
     */
    int getExitCode() {
        return exitCode;
    }
}
