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
import lcm.DirtyLineProvider;
import lcm.DirtyLineProviderOptions;
import org.gjt.sp.jedit.Buffer;

/**
 * Implementation of {@link DirtyLineProvider} that calculates the difference
 * between the current buffer content and the content of the HEAD commit in the
 * Git repository.
 */
public final class GitDirtyLineProvider implements DirtyLineProvider {
    @Override
    public BufferHandler attach(final Buffer buffer) {
        return new GitBufferHandlerAdapter(buffer);
    }

    @Override
    public void detach(final Buffer buffer, final BufferHandler bufferHandler) {
        ((GitBufferHandlerAdapter) bufferHandler).stop();
    }

    @Override
    public DirtyLineProviderOptions getOptions() {
        return new GitDirtyLineProviderOptions();
    }
}
