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

import java.awt.Graphics2D;
import lcm.painters.ColoredRectWithStripsPainter;
import lcm.painters.DirtyMarkPainter;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.textarea.Gutter;

/**
 * A factory for creating dirty mark painters.
 */
final class DirtyMarkPainterFactory {
    private static final DirtyMarkPainter NULL_DIRTY_MARK_PAINTER = new DirtyMarkPainter() {
        @Override
        public void paint(final Graphics2D g, final Gutter gutter, final int y, final int height, final Buffer buffer,
                final int lineIndex) {
            // do nothing
        }
    };

    private DirtyMarkPainterFactory() {
    }

    /**
     * Creates a new dirty mark painter for the specified specification.
     *
     * @param specification
     *        The specification describing the dirty mark painter to be created.
     *
     * @return A new dirty mark painter.
     */
    static DirtyMarkPainter createDirtyMarkPainter(final DirtyMarkPainterSpecification specification) {
        if (specification == DirtyMarkPainterSpecification.NULL) {
            return NULL_DIRTY_MARK_PAINTER;
        }

        final ColoredRectWithStripsPainter dirtyMarkPainter = new ColoredRectWithStripsPainter();

        dirtyMarkPainter.setParts(specification.isTopStripPainted(), specification.isBodyPainted(),
                specification.isBottomStripPainted());

        if (specification.isBodyPainted()) {
            dirtyMarkPainter.setColor(specification.getColor());
        }
        if (specification.isTopStripPainted() || specification.isBottomStripPainted()) {
            dirtyMarkPainter.setStripColor(specification.getColor());
        }

        return dirtyMarkPainter;
    }
}
