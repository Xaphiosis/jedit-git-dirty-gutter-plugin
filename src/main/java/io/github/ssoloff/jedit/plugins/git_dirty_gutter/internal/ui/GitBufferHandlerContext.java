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

import git.GitPlugin;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.IBuffer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.ILog;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.ISupplier;
import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.util.Log;

/**
 * The execution context for the Git buffer handler that provides the inbound
 * bridge to the jEdit API.
 */
final class GitBufferHandlerContext implements IGitBufferHandlerContext {
    //CHECKSTYLEOFF:LineLength
    private static final IDirtyMarkPainterSpecificationFactoryContext DIRTY_MARK_PAINTER_SPECIFICATION_FACTORY_CONTEXT = createDirtyMarkPainterSpecificationFactoryContext();
    //CHECKSTYLEON:LineLength
    private static final ISupplier<Path> GIT_PROGRAM_PATH_SUPPLIER = createGitProgramPathSupplier();
    private static final ILog LOG = createLog();

    private final IBuffer bufferAdapter;

    /**
     * Initializes a new instance of the {@code GitBufferHandlerContext} class.
     *
     * @param buffer
     *        The jEdit buffer associated with the context.
     */
    GitBufferHandlerContext(final Buffer buffer) {
        this.bufferAdapter = createBufferAdapter(buffer);
    }

    private static IBuffer createBufferAdapter(final Buffer buffer) {
        return new IBuffer() {
            @Override
            public List<String> getLines() {
                final int lineCount = buffer.getLineCount();
                final List<String> lines = new ArrayList<>(lineCount);
                for (int lineIndex = 0; lineIndex < lineCount; ++lineIndex) {
                    lines.add(buffer.getLineText(lineIndex));
                }
                return lines;
            }

            @Override
            public Path getFilePath() {
                return Paths.get(buffer.getPath());
            }
        };
    }

    private static IDirtyMarkPainterSpecificationFactoryContext createDirtyMarkPainterSpecificationFactoryContext() {
        return new IDirtyMarkPainterSpecificationFactoryContext() {
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
        };
    }

    private static ISupplier<Path> createGitProgramPathSupplier() {
        return new ISupplier<Path>() {
            @Override
            public Path get() {
                return Paths.get(GitPlugin.gitPath());
            }
        };
    }

    private static ILog createLog() {
        return new ILog() {
            @Override
            public void logDebug(final Object source, final String message) {
                Log.log(Log.DEBUG, source, message);
            }

            @Override
            public void logError(final Object source, final String message, final Throwable t) {
                Log.log(Log.ERROR, source, message, t);
            }

            @Override
            public void logWarning(final Object source, final String message, final Throwable t) {
                Log.log(Log.WARNING, source, message, t);
            }
        };
    }

    @Override
    public IBuffer getBuffer() {
        return bufferAdapter;
    }

    @Override
    public IDirtyMarkPainterSpecificationFactoryContext getDirtyMarkPainterSpecificationFactoryContext() {
        return DIRTY_MARK_PAINTER_SPECIFICATION_FACTORY_CONTEXT;
    }

    @Override
    public ISupplier<Path> getGitProgramPathSupplier() {
        return GIT_PROGRAM_PATH_SUPPLIER;
    }

    @Override
    public ILog getLog() {
        return LOG;
    }

    @Override
    public int getRepositoryPollTimeInMilliseconds() {
        return Properties.getRepositoryPollTimeInMilliseconds();
    }
}
