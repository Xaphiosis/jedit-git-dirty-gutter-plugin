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

import difflib.Patch;
import git.GitPlugin;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.BufferAnalyzer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.IBuffer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.ILog;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.PatchAnalyzer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.AutoResetEvent;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.ISupplier;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.Properties;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.ProcessRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunnerFactory;
import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingWorker;
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
 */
final class GitBufferHandler extends BufferAdapter implements BufferHandler {
    private final Buffer buffer;
    private final DirtyMarkPainterSpecificationFactory dirtyMarkPainterSpecificationFactory = createDirtyMarkPainterSpecificationFactory();
    private @Nullable Patch patch = null;
    private final PatchWorker patchWorker = new PatchWorker();

    /**
     * Initializes a new instance of the {@code GitBufferHandler} class.
     *
     * @param buffer
     *        The associated buffer.
     */
    GitBufferHandler(final Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void bufferSaved(final Buffer unusedBuffer) {
        // do nothing
    }

    @Override
    public void contentInserted(final JEditBuffer unusedBuffer, final int startLine, final int offset,
            final int numLines, final int length) {
        updatePatch();
    }

    @Override
    public void contentRemoved(final JEditBuffer unusedBuffer, final int startLine, final int offset,
            final int numLines, final int length) {
        updatePatch();
    }

    private static DirtyMarkPainterSpecificationFactory createDirtyMarkPainterSpecificationFactory() {
        final IDirtyMarkPainterSpecificationFactoryContext context = new IDirtyMarkPainterSpecificationFactoryContext() {
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
        return new DirtyMarkPainterSpecificationFactory(context);
    }

    @Override
    public @Nullable DirtyMarkPainter getDirtyMarkPainter(final Buffer unusedBuffer, final int lineIndex) {
        final DirtyMarkType dirtyMarkType = getDirtyMarkForLine(lineIndex);
        final DirtyMarkPainterSpecification dirtyMarkPainterSpecification = dirtyMarkPainterSpecificationFactory
                .createDirtyMarkPainterSpecification(dirtyMarkType);
        return DirtyMarkPainterFactory.createDirtyMarkPainter(dirtyMarkPainterSpecification);
    }

    private DirtyMarkType getDirtyMarkForLine(final int lineIndex) {
        @SuppressWarnings("hiding")
        final Patch patch = this.patch;
        if (patch == null) {
            return DirtyMarkType.UNCHANGED;
        }

        final PatchAnalyzer patchAnalyzer = new PatchAnalyzer(patch);
        return patchAnalyzer.getDirtyMarkForLine(lineIndex);
    }

    private void setPatch(final @Nullable Patch patch) {
        this.patch = patch;
        LCMPlugin.getInstance().repaintAllTextAreas();
    }

    @Override
    public void start() {
        startPatchWorker();
        updatePatch();
    }

    private void startPatchWorker() {
        patchWorker.execute();
    }

    /**
     * Invoked when the handler has been detached from the buffer.
     */
    void stop() {
        stopPatchWorker();
    }

    private void stopPatchWorker() {
        patchWorker.cancel(true);
    }

    private void updatePatch() {
        patchWorker.updatePatch();
    }

    /**
     * A background task that is responsible for updating the patch associated
     * with the buffer when requested or when a change in the repository is
     * detected.
     */
    @SuppressWarnings("synthetic-access")
    private final class PatchWorker extends SwingWorker<Void, Patch> {
        private final AutoResetEvent updatePatchEvent = new AutoResetEvent();

        PatchWorker() {
        }

        private BufferAnalyzer createBufferAnalyzer() {
            final IBuffer bufferAdapter = new IBuffer() {
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
            final IGitRunnerFactory gitRunnerFactory = new IGitRunnerFactory() {
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
            final ILog logAdapter = new ILog() {
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
            return new BufferAnalyzer(bufferAdapter, gitRunnerFactory, logAdapter);
        }

        @Override
        protected @Nullable Void doInBackground() throws Exception {
            final BufferAnalyzer bufferAnalyzer = createBufferAnalyzer();
            final AtomicReference<String> commitRefRef = new AtomicReference<>();
            while (true) {
                if (isPatchUpdatePending() || bufferAnalyzer.hasHeadRevisionChanged(commitRefRef)) {
                    publish(bufferAnalyzer.createPatchBetweenHeadRevisionAndCurrentState());
                }
            }
        }

        private boolean isPatchUpdatePending() throws InterruptedException {
            return updatePatchEvent.await(Properties.getCommitMonitorPollTime(), TimeUnit.MILLISECONDS);
        }

        @Override
        protected void process(final List<Patch> patches) {
            assert patches.size() == 1;
            setPatch(patches.get(0));
        }

        void updatePatch() {
            updatePatchEvent.signal();
        }
    }
}
