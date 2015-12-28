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

import difflib.DiffUtils;
import difflib.Patch;
import git.GitPlugin;
import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
    private GitCommands createGitCommands() throws IOException {
        final Path filePath = getFilePath();
        final Path workingDirPath = filePath.getParent();
        if (workingDirPath == null) {
            throw new IOException(String.format("unable to get directory for '%s'", filePath)); //$NON-NLS-1$
        }
        final IGitRunner gitRunner = new GitRunner(new ProcessRunner(), Paths.get(GitPlugin.gitPath()), workingDirPath);
        return new GitCommands(gitRunner);
    }

    private List<String> getBufferLines() {
        final int lineCount = buffer.getLineCount();
        final List<String> lines = new ArrayList<>(lineCount);
        for (int lineIndex = 0; lineIndex < lineCount; ++lineIndex) {
            lines.add(buffer.getLineText(lineIndex));
        }
        return lines;
    }

    /*
     * This method is thread-safe.
     */
    private @Nullable String getCommitRefAtHeadRevision() throws GitException, IOException, InterruptedException {
        final GitCommands gitCommands = createGitCommands();
        final Path repoRelativeFilePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(getFilePath());
        if (repoRelativeFilePath == null) {
            return null;
        }
        return gitCommands.getCommitRefAtHeadRevision(repoRelativeFilePath);
    }

    /*
     * This method is thread-safe.
     */
    private Path getFilePath() {
        return Paths.get(buffer.getPath());
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

    private @Nullable List<String> getHeadRevisionLines() {
        final Path filePath = getFilePath();
        try {
            final GitCommands gitCommands = createGitCommands();

            final Path repoRelativeFilePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(filePath);
            if (repoRelativeFilePath == null) {
                Log.log(Log.DEBUG, this, String.format("'%s' does not exist in Git repository", filePath)); //$NON-NLS-1$
                return null;
            }

            final StringWriter headRevisionFileWriter = new StringWriter();
            gitCommands.readFileContentAtHeadRevision(repoRelativeFilePath, headRevisionFileWriter);
            return StringUtils.splitLinesWithExplicitFinalLine(headRevisionFileWriter.getBuffer());
        } catch (final IOException | GitException e) {
            Log.log(Log.ERROR, this, String.format("failed to get HEAD revision of file '%s'", filePath), e); //$NON-NLS-1$
            return null;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.log(Log.NOTICE, this, String.format("interrupted while getting HEAD revision of file '%s'", filePath), //$NON-NLS-1$
                    e);
            return null;
        }
    }

    /*
     * This method is thread-safe.
     */
    private boolean isDirtyMarkProcessingEnabled() {
        try {
            if (buffer.isNewFile()) {
                return false;
            }

            final GitCommands gitCommands = createGitCommands();
            return gitCommands.isInsideRepo();
        } catch (final GitException | IOException e) {
            Log.log(Log.ERROR, this, String.format("failed to determine if dirty mark processing enabled for file '%s'", //$NON-NLS-1$
                    getFilePath()), e);
            return false;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.log(Log.NOTICE, this,
                    String.format("interrupted while determining if dirty mark processing enabled for file '%s'", //$NON-NLS-1$
                            getFilePath()),
                    e);
            return false;
        }
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
        patch = null;

        if (!isDirtyMarkProcessingEnabled()) {
            Log.log(Log.DEBUG, this, String.format("dirty mark processing not enabled for file '%s'", getFilePath())); //$NON-NLS-1$
            return;
        }

        final @Nullable List<String> headRevisionLines = getHeadRevisionLines();
        if (headRevisionLines != null) {
            patch = DiffUtils.diff(headRevisionLines, getBufferLines());
            LCMPlugin.getInstance().repaintAllTextAreas();
        }
    }

    /**
     * A background task to monitor the repository to detect when the file has
     * been committed.
     */
    @SuppressWarnings("synthetic-access")
    private final class CommitMonitorWorker extends SwingWorker<Void, Void> {
        @Override
        protected @Nullable Void doInBackground() throws Exception {
            String previousCommitRef = null;

            while (true) {
                try {
                    if (isDirtyMarkProcessingEnabled()) {
                        final String currentCommitRef = getCommitRefAtHeadRevision();
                        if ((currentCommitRef != null) && (previousCommitRef != currentCommitRef)) {
                            previousCommitRef = currentCommitRef;
                            publish();
                        }
                    } else {
                        previousCommitRef = null;
                    }

                    Thread.sleep(Properties.getCommitMonitorPollTime());
                } catch (final GitException | IOException e) {
                    Log.log(Log.ERROR, this,
                            String.format("failed to get commit ref for HEAD revision of file '%s'", getFilePath()), e); //$NON-NLS-1$
                }
            }
        }

        @Override
        protected void process(final List<Void> chunks) {
            updatePatch();
        }
    }
}
