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

import git.GitPlugin;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.IBuffer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.ILog;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.ISupplier;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.ProcessRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunnerFactory;
import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lcm.BufferHandler;
import lcm.LCMPlugin;
import lcm.painters.DirtyMarkPainter;
import org.eclipse.jdt.annotation.Nullable;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.buffer.BufferAdapter;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.util.Log;

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

    private static final class GitBufferHandlerContext implements IGitBufferHandlerContext {
        private static final IDirtyMarkPainterSpecificationFactoryContext dirtyMarkPainterSpecificationFactoryContext = createDirtyMarkPainterSpecificationFactoryContext();
        private static final IGitRunnerFactory gitRunnerFactory = createGitRunnerFactory();
        private static final ILog log = createLog();

        private final IBuffer bufferAdapter;

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

        private static IGitRunnerFactory createGitRunnerFactory() {
            return new IGitRunnerFactory() {
                @Override
                public IGitRunner createGitRunner(final Path workingDirPath) {
                    final ISupplier<Path> programPathSupplier = new ISupplier<Path>() {
                        @Override
                        public Path get() {
                            return Paths.get(GitPlugin.gitPath());
                        }
                    };
                    return new GitRunner(new ProcessRunner(), workingDirPath, programPathSupplier);
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
        public int getCommitMonitorPollTimeInMilliseconds() {
            return Properties.getCommitMonitorPollTime();
        }

        @Override
        public IDirtyMarkPainterSpecificationFactoryContext getDirtyMarkPainterSpecificationFactoryContext() {
            return dirtyMarkPainterSpecificationFactoryContext;
        }

        @Override
        public IGitRunnerFactory getGitRunnerFactory() {
            return gitRunnerFactory;
        }

        @Override
        public ILog getLog() {
            return log;
        }
    }

    private static final class GitBufferHandlerListener implements IGitBufferHandlerListener {
        GitBufferHandlerListener() {
        }

        @Override
        public void patchUpdated() {
            LCMPlugin.getInstance().repaintAllTextAreas();
        }
    }
}
