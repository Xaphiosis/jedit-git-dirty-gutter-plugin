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

import lcm.BufferHandler;
import lcm.painters.ColoredRectWithStripsPainter;
import lcm.painters.DirtyMarkPainter;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.buffer.BufferAdapter;

/**
 * Implementation of {@link BufferHandler} for the Git dirty line provider.
 */
final class GitBufferHandler extends BufferAdapter implements BufferHandler {
    /*
     * @see lcm.BufferHandler#bufferSaved(org.gjt.sp.jedit.Buffer)
     */
    @Override
    public void bufferSaved(final Buffer buffer) {
        // TODO
    }

    /*
     * @see lcm.BufferHandler#getDirtyMarkPainter(org.gjt.sp.jedit.Buffer, int)
     */
    @Override
    public DirtyMarkPainter getDirtyMarkPainter(final Buffer buffer, final int physicalLine) {
        // TODO
        return new ColoredRectWithStripsPainter();
    }

    /*
     * @see lcm.BufferHandler#start()
     */
    @Override
    public void start() {
        // do nothing
    }
}
