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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.ui;

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.IBuffer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.ILog;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunnerFactory;

/**
 * The execution context for an instance of {@code GitBufferHandler}.
 *
 * <p>
 * The purpose of this interface is to encapsulate the jEdit-specific APIs from
 * the buffer handler to facilitate testing.
 * </p>
 */
interface IGitBufferHandlerContext {
    /**
     * Gets the buffer.
     *
     * @return The buffer.
     */
    IBuffer getBuffer();

    /**
     * Gets the execution context for the dirty mark painter specification
     * factory.
     *
     * @return The execution context for the dirty mark painter specification
     *         factory.
     */
    IDirtyMarkPainterSpecificationFactoryContext getDirtyMarkPainterSpecificationFactoryContext();

    /**
     * Gets the Git runner factory.
     *
     * @return The Git runner factory.
     */
    IGitRunnerFactory getGitRunnerFactory();

    /**
     * Gets the log.
     *
     * @return The log.
     */
    ILog getLog();

    /**
     * Gets the time (in milliseconds) between polling the Git repository for
     * new commits.
     *
     * @return The time (in milliseconds) between polling the Git repository for
     *         new commits.
     */
    int getRepositoryPollTimeInMilliseconds();
}
