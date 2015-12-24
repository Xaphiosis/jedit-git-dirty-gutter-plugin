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

import java.awt.Color;
import lcm.BufferHandler;
import lcm.painters.DirtyMarkPainter;
import org.eclipse.jdt.annotation.Nullable;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.buffer.BufferAdapter;

/**
 * Implementation of {@link BufferHandler} for the Git dirty line provider.
 */
final class GitBufferHandler extends BufferAdapter implements BufferHandler {
    private final DirtyMarkPainterFactory dirtyMarkPainterFactory = createDirtyMarkPainterFactory();

    /*
     * @see lcm.BufferHandler#bufferSaved(org.gjt.sp.jedit.Buffer)
     */
    @Override
    public void bufferSaved(final @Nullable Buffer buffer) {
        // do nothing
    }

    private static DirtyMarkPainterFactory createDirtyMarkPainterFactory() {
        return new DirtyMarkPainterFactory(new DirtyMarkPainterFactoryContext() {
            @Override
            public Color getAddedDirtyMarkColor() {
                return Properties.getAddedDirtyMarkColor();
            }

            @Override
            public Color getChangedDirtyMarkColor() {
                return Properties.getChangedDirtyMarkColor();
            }

            @Override
            public Color getRemovedDirtyMarkColor() {
                return Properties.getRemovedDirtyMarkColor();
            }
        });
    }

    /*
     * @see lcm.BufferHandler#getDirtyMarkPainter(org.gjt.sp.jedit.Buffer, int)
     */
    @Override
    public DirtyMarkPainter getDirtyMarkPainter(final @Nullable Buffer buffer, final int lineIndex) {
        // TODO
        return dirtyMarkPainterFactory.createDirtyMarkPainter(DirtyMarkType.ADDED);
    }

    /*
     * @see lcm.BufferHandler#start()
     */
    @Override
    public void start() {
        // do nothing
    }
}
