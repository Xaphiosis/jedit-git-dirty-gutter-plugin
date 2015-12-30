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

import difflib.Patch;
import git.GitPlugin;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.git.GitRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.git.GitTasks;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.git.IGitRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.git.IGitRunnerFactory;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.IBuffer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.PatchAnalyzer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.ui.DirtyMarkPainterFactory;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.ui.IDirtyMarkPainterFactoryContext;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.ILog;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.Properties;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.ProcessRunner;
import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
    private @Nullable CommitMonitorWorker commitMonitorWorker = null;
    private final DirtyMarkPainterFactory dirtyMarkPainterFactory = createDirtyMarkPainterFactory();
    private @Nullable Patch patch = null;

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

    /*
     * This method is thread-safe.
     * The returned {@code IBuffer} is thread-safe.
     */
    private IBuffer createBufferAdapter() {
        @SuppressWarnings("synthetic-access")
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
        return bufferAdapter;
    }

    private static DirtyMarkPainterFactory createDirtyMarkPainterFactory() {
        return new DirtyMarkPainterFactory(new IDirtyMarkPainterFactoryContext() {
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
     * This method is thread-safe.
     */
    private static IGitRunnerFactory createGitRunnerFactory() {
        return new IGitRunnerFactory() {
            @Override
            public IGitRunner createGitRunner(final Path workingDirPath) {
                return new GitRunner(new ProcessRunner(), Paths.get(GitPlugin.gitPath()), workingDirPath);
            }
        };
    }

    /*
     * This method is thread-safe.
     */
    private static ILog createLogAdapter() {
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
    public @Nullable DirtyMarkPainter getDirtyMarkPainter(final Buffer unusedBuffer, final int lineIndex) {
        @SuppressWarnings("hiding")
        final Patch patch = this.patch;
        if (patch == null) {
            return null;
        }

        final PatchAnalyzer patchAnalyzer = new PatchAnalyzer(patch);
        final DirtyMarkType dirtyMarkType = patchAnalyzer.getDirtyMarkForLine(lineIndex);
        return dirtyMarkPainterFactory.createDirtyMarkPainter(dirtyMarkType);
    }

    @Override
    public void start() {
        startCommitMonitor();
        updatePatch();
    }

    private void startCommitMonitor() {
        assert this.commitMonitorWorker == null;

        @SuppressWarnings({
            "hiding", "synthetic-access"
        })
        final CommitMonitorWorker commitMonitorWorker = new CommitMonitorWorker();
        this.commitMonitorWorker = commitMonitorWorker;
        commitMonitorWorker.execute();
    }

    /**
     * Invoked when the handler has been detached from the buffer.
     */
    void stop() {
        stopCommitMonitor();
    }

    private void stopCommitMonitor() {
        @SuppressWarnings("hiding")
        final CommitMonitorWorker commitMonitorWorker = this.commitMonitorWorker;
        if (commitMonitorWorker != null) {
            this.commitMonitorWorker = null;
            commitMonitorWorker.cancel(true);
        }
    }

    private void updatePatch() {
        // TODO: try-catch temporary until we move running task to a SwingWorker
        try {
            final GitTasks gitTasks = new GitTasks(createGitRunnerFactory(), createLogAdapter());
            patch = gitTasks.createPatchBetweenHeadRevisionAndCurrentState(createBufferAdapter());
        } catch (@SuppressWarnings("unused") final InterruptedException e) {
            Thread.currentThread().interrupt();
            patch = null;
        }

        LCMPlugin.getInstance().repaintAllTextAreas();
    }

    /**
     * A background task to monitor the repository to detect when the file has
     * been committed.
     */
    @SuppressWarnings("synthetic-access")
    private final class CommitMonitorWorker extends SwingWorker<Void, Void> {
        @Override
        protected @Nullable Void doInBackground() throws Exception {
            final AtomicReference<String> commitRefRef = new AtomicReference<>();
            final IBuffer bufferAdapter = createBufferAdapter();

            while (true) {
                final GitTasks gitTasks = new GitTasks(createGitRunnerFactory(), createLogAdapter());
                if (gitTasks.hasHeadRevisionChanged(bufferAdapter, commitRefRef)) {
                    publish();
                }

                Thread.sleep(Properties.getCommitMonitorPollTime());
            }
        }

        @Override
        protected void process(final List<Void> chunks) {
            updatePatch();
        }
    }
}
