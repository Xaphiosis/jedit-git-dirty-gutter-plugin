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
 * The type of dirty mark associated with a line.
 */
enum DirtyMarkType {
    /** The line was added since the last commit. */
    ADDED,

    /** The line was changed since the last commit. */
    CHANGED,

    /**
     * One or more lines have been removed above the line since the last commit.
     */
    REMOVED_ABOVE,

    /**
     * One or more lines have been removed above the line and one or more lines
     * have been removed below the line since the last commit.
     */
    REMOVED_ABOVE_AND_BELOW,

    /**
     * One or more lines have been removed below the line since the last commit.
     */
    REMOVED_BELOW,

    /** The line has not changed since the last commit. */
    UNCHANGED;
}
