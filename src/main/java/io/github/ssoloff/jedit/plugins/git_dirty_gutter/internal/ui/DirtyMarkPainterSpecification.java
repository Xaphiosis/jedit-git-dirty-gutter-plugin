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

import java.awt.Color;

/**
 * A specification for how to paint a dirty mark.
 */
final class DirtyMarkPainterSpecification {
    /** A specification that indicates a dirty mark should not be painted. */
    static final DirtyMarkPainterSpecification NULL = new DirtyMarkPainterSpecification(Color.BLACK, false, false,
            false);

    private final Color color;
    private final boolean isBodyPainted;
    private final boolean isBottomStripPainted;
    private final boolean isTopStripPainted;

    /**
     * Initializes a new instance of the {@code DirtyMarkPainterSpecification}
     * class.
     *
     * @param color
     *        The color to paint the dirty mark.
     * @param isTopStripPainted
     *        {@code true} if the top strip of the dirty mark is to be painted;
     *        otherwise {@code false}.
     * @param isBodyPainted
     *        {@code true} if the body of the dirty mark is to be painted;
     *        otherwise {@code false}.
     * @param isBottomStripPainted
     *        {@code true} if the bottom strip of the dirty mark is to be
     *        painted; otherwise {@code false}.
     */
    DirtyMarkPainterSpecification(final Color color, final boolean isTopStripPainted, final boolean isBodyPainted,
            final boolean isBottomStripPainted) {
        this.color = color;
        this.isBodyPainted = isBodyPainted;
        this.isBottomStripPainted = isBottomStripPainted;
        this.isTopStripPainted = isTopStripPainted;
    }

    /**
     * Gets the color to paint the dirty mark.
     *
     * @return The color to paint the dirty mark.
     */
    Color getColor() {
        return color;
    }

    /**
     * Indicates the body of the dirty mark is to be painted.
     *
     * @return {@code true} if the body of the dirty mark is to be painted;
     *         otherwise {@code false}.
     */
    boolean isBodyPainted() {
        return isBodyPainted;
    }

    /**
     * Indicates the bottom strip of the dirty mark is to be painted.
     *
     * @return {@code true} if the bottom strip of the dirty mark is to be
     *         painted; otherwise {@code false}.
     */
    boolean isBottomStripPainted() {
        return isBottomStripPainted;
    }

    /**
     * Indicates the top strip of the dirty mark is to be painted.
     *
     * @return {@code true} if the top strip of the dirty mark is to be painted;
     *         otherwise {@code false}.
     */
    boolean isTopStripPainted() {
        return isTopStripPainted;
    }
}
