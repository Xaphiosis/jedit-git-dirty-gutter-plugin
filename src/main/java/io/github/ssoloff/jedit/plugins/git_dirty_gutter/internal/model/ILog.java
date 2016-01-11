/*
 * Copyright (C) 2016 Steven Soloff
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

/**
 * The jEdit log.
 */
@SuppressWarnings("PMD.ShortClassName")
public interface ILog {
    /**
     * Logs a message at the debug level of urgency.
     *
     * @param source
     *        The source of the message.
     * @param message
     *        The message.
     */
    void logDebug(Object source, String message);

    /**
     * Logs a message at the error level of urgency.
     *
     * @param source
     *        The source of the message.
     * @param message
     *        The message.
     * @param t
     *        The cause of the error.
     */
    void logError(Object source, String message, Throwable t);

    /**
     * Logs a message at the warning level of urgency.
     *
     * @param source
     *        The source of the message.
     * @param message
     *        The message.
     * @param t
     *        The cause of the warning.
     */
    void logWarning(Object source, String message, Throwable t);
}
