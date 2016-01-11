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

import difflib.Patch;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.BufferAnalyzer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.PatchAnalyzer;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.AutoResetEvent;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.ProcessRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunner;
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunnerFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Provides the underlying implementation of {@code BufferHandler} for the Git
 * dirty line provider.
 */
@SuppressWarnings("PMD.TooManyMethods")
final class GitBufferHandler {
    private final DirtyMarkPainterSpecificationFactory dirtyMarkPainterSpecificationFactory;
    private final IGitBufferHandlerContext context;
    private final List<IGitBufferHandlerListener> listeners = new ArrayList<>();
    private Patch patch = new Patch();
    private final PatchWorker patchWorker = new PatchWorker();

    /**
     * Initializes a new instance of the {@code GitBufferHandler} class.
     *
     * @param context
     *        The execution context for the handler.
     */
    GitBufferHandler(final IGitBufferHandlerContext context) {
        assert SwingUtilities.isEventDispatchThread();

        this.context = context;
        this.dirtyMarkPainterSpecificationFactory = new DirtyMarkPainterSpecificationFactory(
                context.getDirtyMarkPainterSpecificationFactoryContext());
    }

    /**
     * Adds the specified listener to the buffer handler.
     *
     * @param listener
     *        The listener to add.
     */
    void addListener(final IGitBufferHandlerListener listener) {
        assert SwingUtilities.isEventDispatchThread();

        listeners.add(listener);
    }

    private DirtyMarkType getDirtyMarkForLine(final int lineIndex) {
        final PatchAnalyzer patchAnalyzer = new PatchAnalyzer(patch);
        return patchAnalyzer.getDirtyMarkForLine(lineIndex);
    }

    /**
     * Gets the dirty mark painter specification for the specified line.
     *
     * @param lineIndex
     *        The zero-based index of the line for which the dirty mark painter
     *        specification is desired.
     *
     * @return The dirty mark painter specification for the specified line.
     */
    DirtyMarkPainterSpecification getDirtyMarkPainterSpecificationForLine(final int lineIndex) {
        assert SwingUtilities.isEventDispatchThread();

        final DirtyMarkType dirtyMarkType = getDirtyMarkForLine(lineIndex);
        return dirtyMarkPainterSpecificationFactory.createDirtyMarkPainterSpecification(dirtyMarkType);
    }

    private void raisePatchUpdatedEvent() {
        for (final IGitBufferHandlerListener listener : listeners) {
            listener.patchUpdated();
        }
    }

    /**
     * Removes the specified listener from the buffer handler.
     *
     * @param listener
     *        The listener to remove.
     */
    void removeListener(final IGitBufferHandlerListener listener) {
        assert SwingUtilities.isEventDispatchThread();

        listeners.remove(listener);
    }

    private void setPatch(final Patch patch) {
        this.patch = patch;
        raisePatchUpdatedEvent();
    }

    /**
     * Starts the buffer handler.
     *
     * <p>
     * This method should be invoked immediately after attaching the buffer.
     * </p>
     */
    void start() {
        assert SwingUtilities.isEventDispatchThread();

        startPatchWorker();
        updatePatch();
    }

    private void startPatchWorker() {
        patchWorker.execute();
    }

    /**
     * Stops the buffer handler.
     *
     * <p>
     * This method should be invoked immediately after detaching the buffer.
     * </p>
     */
    void stop() {
        assert SwingUtilities.isEventDispatchThread();

        stopPatchWorker();
    }

    private void stopPatchWorker() {
        patchWorker.cancel(true);
    }

    /**
     * Requests the buffer patch to be updated.
     *
     * <p>
     * This method returns immediately. When the patch update is complete, the
     * dirty gutter will be repainted asynchronously.
     * </p>
     */
    void updatePatch() {
        assert SwingUtilities.isEventDispatchThread();

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
            // do nothing
        }

        private BufferAnalyzer createBufferAnalyzer() {
            final IGitRunnerFactory gitRunnerFactory = new IGitRunnerFactory() {
                @Override
                public IGitRunner createGitRunner(final Path workingDirPath) {
                    return new GitRunner(new ProcessRunner(), workingDirPath, context.getGitProgramPathSupplier());
                }
            };
            return new BufferAnalyzer(context.getBuffer(), gitRunnerFactory, context.getLog());
        }

        @Nullable
        @Override
        protected Void doInBackground() throws InterruptedException {
            final BufferAnalyzer bufferAnalyzer = createBufferAnalyzer();
            final AtomicReference<String> commitRefRef = new AtomicReference<>();
            while (true) {
                if (isPatchUpdatePending() || bufferAnalyzer.hasHeadRevisionChanged(commitRefRef)) {
                    publish(bufferAnalyzer.createPatchBetweenHeadRevisionAndCurrentState());
                }
            }
        }

        private boolean isPatchUpdatePending() throws InterruptedException {
            return updatePatchEvent.await(context.getRepositoryPollTimeInMilliseconds(), TimeUnit.MILLISECONDS);
        }

        @Override
        protected void process(final List<Patch> patches) {
            final int patchCount = patches.size();
            if (patchCount > 0) {
                // discard all but the latest patch if multiple patches pending
                final Patch latestPatch = patches.get(patchCount - 1);
                assert latestPatch != null;
                setPatch(latestPatch);
            }
        }

        void updatePatch() {
            updatePatchEvent.signal();
        }
    }
}
