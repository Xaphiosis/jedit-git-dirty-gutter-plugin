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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.ui;

import java.awt.Color;
import org.gjt.sp.jedit.jEdit;

/**
 * Provides access to the plugin properties.
 *
 * <p>
 * The methods of this class are thread-safe.
 * </p>
 */
final class Properties {
    private static final String PROP_PREFIX = "io.github.ssoloff.jedit.plugins.git_dirty_gutter.GitDirtyGutterPlugin."; //$NON-NLS-1$
    private static final String PROP_ADDED_DIRTY_MARK_COLOR = PROP_PREFIX + "addedDirtyMarkColor"; //$NON-NLS-1$
    private static final String PROP_CHANGED_DIRTY_MARK_COLOR = PROP_PREFIX + "changedDirtyMarkColor"; //$NON-NLS-1$
    private static final String PROP_REMOVED_DIRTY_MARK_COLOR = PROP_PREFIX + "removedDirtyMarkColor"; //$NON-NLS-1$
    private static final String PROP_REPOSITORY_POLL_TIME_IN_MILLISECONDS = PROP_PREFIX
            + "repositoryPollTimeInMilliseconds"; //$NON-NLS-1$

    private Properties() {
    }

    /**
     * Gets the color used for added dirty marks.
     *
     * @return The color used for added dirty marks.
     */
    static Color getAddedDirtyMarkColor() {
        return jEdit.getColorProperty(PROP_ADDED_DIRTY_MARK_COLOR, Color.GREEN);
    }

    /**
     * Gets the color used for changed dirty marks.
     *
     * @return The color used for changed dirty marks.
     */
    static Color getChangedDirtyMarkColor() {
        return jEdit.getColorProperty(PROP_CHANGED_DIRTY_MARK_COLOR, Color.ORANGE);
    }

    /**
     * Gets the color used for removed dirty marks.
     *
     * @return The color used for removed dirty marks.
     */
    static Color getRemovedDirtyMarkColor() {
        return jEdit.getColorProperty(PROP_REMOVED_DIRTY_MARK_COLOR, Color.RED);
    }

    /**
     * Gets the time (in milliseconds) between polling the Git repository for
     * new commits.
     *
     * @return The time (in milliseconds) between polling the Git repository for
     *         new commits.
     */
    static int getRepositoryPollTimeInMilliseconds() {
        final int defaultRepositoryPollTimeInMilliseconds = 5000;
        return jEdit.getIntegerProperty(PROP_REPOSITORY_POLL_TIME_IN_MILLISECONDS,
                defaultRepositoryPollTimeInMilliseconds);
    }

    /**
     * Sets the color used for added dirty marks.
     *
     * @param addedDirtyMarkColor
     *        The color used for added dirty marks.
     */
    static void setAddedDirtyMarkColor(final Color addedDirtyMarkColor) {
        jEdit.setColorProperty(PROP_ADDED_DIRTY_MARK_COLOR, addedDirtyMarkColor);
    }

    /**
     * Sets the color used for changed dirty marks.
     *
     * @param changedDirtyMarkColor
     *        The color used for changed dirty marks.
     */
    static void setChangedDirtyMarkColor(final Color changedDirtyMarkColor) {
        jEdit.setColorProperty(PROP_CHANGED_DIRTY_MARK_COLOR, changedDirtyMarkColor);
    }

    /**
     * Sets the color used for removed dirty marks.
     *
     * @param removedDirtyMarkColor
     *        The color used for removed dirty marks.
     */
    static void setRemovedDirtyMarkColor(final Color removedDirtyMarkColor) {
        jEdit.setColorProperty(PROP_REMOVED_DIRTY_MARK_COLOR, removedDirtyMarkColor);
    }
}
