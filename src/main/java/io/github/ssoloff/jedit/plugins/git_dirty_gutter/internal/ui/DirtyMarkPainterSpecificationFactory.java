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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.ui;

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType;
import java.awt.Color;

/**
 * A factory for creating dirty mark painter specifications.
 */
final class DirtyMarkPainterSpecificationFactory {
    private final IDirtyMarkPainterSpecificationFactoryContext context;

    /**
     * Initializes a new instance of the
     * {@code DirtyMarkPainterSpecificationFactory} class.
     *
     * @param context
     *        The execution context for the factory.
     */
    DirtyMarkPainterSpecificationFactory(final IDirtyMarkPainterSpecificationFactoryContext context) {
        this.context = context;
    }

    /**
     * Creates a new dirty mark painter specification for the specified type of
     * dirty mark.
     *
     * @param dirtyMarkType
     *        The type of dirty mark for which a painter specification is
     *        desired.
     *
     * @return A new dirty mark painter specification or
     *         {@link DirtyMarkPainterSpecification#NULL} if the specified type
     *         of dirty mark should not be painted.
     */
    DirtyMarkPainterSpecification createDirtyMarkPainterSpecification(final DirtyMarkType dirtyMarkType) {
        if (dirtyMarkType == DirtyMarkType.UNCHANGED) {
            return DirtyMarkPainterSpecification.NULL;
        }

        return new DirtyMarkPainterSpecification(getColor(dirtyMarkType), isTopStripPainted(dirtyMarkType),
                isBodyPainted(dirtyMarkType), isBottomStripPainted(dirtyMarkType));
    }

    private Color getColor(final DirtyMarkType dirtyMarkType) {
        switch (dirtyMarkType) {
            case ADDED:
                return context.getAddedDirtyMarkColor();

            case CHANGED:
                return context.getChangedDirtyMarkColor();

            case REMOVED_ABOVE:
            case REMOVED_ABOVE_AND_BELOW:
            case REMOVED_BELOW:
                return context.getRemovedDirtyMarkColor();

            default:
                throw new AssertionError("unsupported dirty mark type"); //$NON-NLS-1$
        }
    }

    private static boolean isBodyPainted(final DirtyMarkType dirtyMarkType) {
        return (dirtyMarkType == DirtyMarkType.ADDED) || (dirtyMarkType == DirtyMarkType.CHANGED);
    }

    private static boolean isBottomStripPainted(final DirtyMarkType dirtyMarkType) {
        return (dirtyMarkType == DirtyMarkType.REMOVED_BELOW)
                || (dirtyMarkType == DirtyMarkType.REMOVED_ABOVE_AND_BELOW);
    }

    private static boolean isTopStripPainted(final DirtyMarkType dirtyMarkType) {
        return (dirtyMarkType == DirtyMarkType.REMOVED_ABOVE)
                || (dirtyMarkType == DirtyMarkType.REMOVED_ABOVE_AND_BELOW);
    }
}
