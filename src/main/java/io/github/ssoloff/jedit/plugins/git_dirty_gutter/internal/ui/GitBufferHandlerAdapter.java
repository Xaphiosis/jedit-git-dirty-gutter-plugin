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

import lcm.BufferHandler;
import lcm.painters.DirtyMarkPainter;
import org.eclipse.jdt.annotation.Nullable;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.buffer.BufferAdapter;
import org.gjt.sp.jedit.buffer.JEditBuffer;

/**
 * Implementation of {@link BufferHandler} for the Git dirty line provider.
 *
 * <p>
 * This class acts as an adapter between the underlying implementation in
 * {@link GitBufferHandler} that uses the plugin's domain model and the jEdit
 * domain model.
 * </p>
 */
final class GitBufferHandlerAdapter extends BufferAdapter implements BufferHandler {
    private final GitBufferHandler bufferHandler;
    private final IGitBufferHandlerListener bufferHandlerListener = new GitBufferHandlerListener();

    /**
     * Initializes a new instance of the {@code GitBufferHandlerAdapter} class.
     *
     * @param buffer
     *        The associated buffer.
     */
    GitBufferHandlerAdapter(final Buffer buffer) {
        bufferHandler = new GitBufferHandler(new GitBufferHandlerContext(buffer));
    }

    @Override
    public void bufferSaved(final Buffer buffer) {
        // do nothing
    }

    @Override
    public void contentInserted(final JEditBuffer buffer, final int startLine, final int offset, final int numLines,
            final int length) {
        bufferHandler.updatePatch();
    }

    @Override
    public void contentRemoved(final JEditBuffer buffer, final int startLine, final int offset, final int numLines,
            final int length) {
        bufferHandler.updatePatch();
    }

    @Override
    public @Nullable DirtyMarkPainter getDirtyMarkPainter(final Buffer buffer, final int lineIndex) {
        final DirtyMarkPainterSpecification dirtyMarkPainterSpecification = bufferHandler
                .getDirtyMarkPainterSpecificationForLine(lineIndex);
        return DirtyMarkPainterFactory.createDirtyMarkPainter(dirtyMarkPainterSpecification);
    }

    @Override
    public void start() {
        bufferHandler.addListener(bufferHandlerListener);
        bufferHandler.start();
    }

    /**
     * Invoked when the handler has been detached from the buffer.
     */
    void stop() {
        bufferHandler.stop();
        bufferHandler.removeListener(bufferHandlerListener);
    }
}
