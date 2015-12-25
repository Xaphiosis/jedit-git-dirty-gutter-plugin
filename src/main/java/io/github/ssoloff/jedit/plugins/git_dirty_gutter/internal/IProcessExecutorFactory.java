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

/**
 * A factory for creating instances of {@link ProcessExecutor}.
 */
interface IProcessExecutorFactory {
    /**
     * Creates a new process executor.
     *
     * @param command
     *        The command to run. The first element is the process executable.
     *        The remaining elements are the process arguments.
     *
     * @return A new process executor.
     */
    ProcessExecutor createProcessExecutor(String... command);
}
