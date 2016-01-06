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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.ui

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.ILog
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.test.GitIntegrationSpecification
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.AutoResetEvent
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.ISupplier
import java.awt.Color
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

class GitBufferHandlerIntegrationSpec extends GitIntegrationSpecification {
    private static ADDED_DIRTY_MARK_COLOR = Color.GREEN
    private static CHANGED_DIRTY_MARK_COLOR = Color.ORANGE
    private static REMOVED_DIRTY_MARK_COLOR = Color.RED

    private bufferHandler
    private bufferHandlerListenerEvent = new AutoResetEvent()
    private bufferHandlerListener = { bufferHandlerListenerEvent.signal() }

    private createAndStartBufferHandler(filePath) {
        SwingUtilities.invokeAndWait({
            bufferHandler = createBufferHandlerForFile(filePath)
            bufferHandler.addListener(bufferHandlerListener)
            bufferHandler.start()
        })
    }

    private createBufferHandlerForFile(filePath) {
        def buffer = createBufferForFile(filePath)
        def dirtyMarkPainterSpecificationFactoryContext = Stub(IDirtyMarkPainterSpecificationFactoryContext) {
            getAddedDirtyMarkColor() >> ADDED_DIRTY_MARK_COLOR
            getChangedDirtyMarkColor() >> CHANGED_DIRTY_MARK_COLOR
            getRemovedDirtyMarkColor() >> REMOVED_DIRTY_MARK_COLOR
        }
        def log = Stub(ILog)
        def context = Stub(IGitBufferHandlerContext) {
            getBuffer() >> buffer
            getDirtyMarkPainterSpecificationFactoryContext() >> dirtyMarkPainterSpecificationFactoryContext
            getGitProgramPathSupplier() >> ({ getGitProgramPath() } as ISupplier<Path>)
            getLog() >> log
            getRepositoryPollTimeInMilliseconds() >> 500
        }
        new GitBufferHandler(context)
    }

    private getDirtyMarkPainterSpecificationForLine(lineIndex) {
        def dirtyMarkPainterSpecification = null
        SwingUtilities.invokeAndWait({
            dirtyMarkPainterSpecification = bufferHandler.getDirtyMarkPainterSpecificationForLine(lineIndex)
        })
        dirtyMarkPainterSpecification
    }

    private void matchesChangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification) {
        assert dirtyMarkPainterSpecification.color == CHANGED_DIRTY_MARK_COLOR
    }

    private void matchesUnchangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification) {
        assert dirtyMarkPainterSpecification == DirtyMarkPainterSpecification.NULL
    }

    private requestPatchUpdate() {
        SwingUtilities.invokeAndWait({
            bufferHandler.updatePatch()
        })
    }

    private stopBufferHandler() {
        SwingUtilities.invokeAndWait({
            bufferHandler.stop()
            bufferHandler.removeListener(bufferHandlerListener)
        })
    }

    private waitForPatchUpdateNotification() {
        bufferHandlerListenerEvent.await(30, TimeUnit.SECONDS)
    }

    def 'when buffer does not differ from HEAD revision at start it should not report dirty lines'() {
        setup:
        def filePath = repoPath.resolve('file')
        touchFile(filePath, 'line 1\n')
        addAndCommitFile(filePath)

        when:
        createAndStartBufferHandler(filePath)
        waitForPatchUpdateNotification()
        def dirtyMarkPainterSpecification = getDirtyMarkPainterSpecificationForLine(0)

        then:
        matchesUnchangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification)

        cleanup:
        stopBufferHandler()
    }

    def 'when buffer differs from HEAD revision at start it should report dirty lines'() {
        setup:
        def filePath = repoPath.resolve('file')
        touchFile(filePath, 'line 1\n')
        addAndCommitFile(filePath)
        touchFile(filePath, 'new line 1\n')

        when:
        createAndStartBufferHandler(filePath)
        waitForPatchUpdateNotification()
        def dirtyMarkPainterSpecification = getDirtyMarkPainterSpecificationForLine(0)

        then:
        matchesChangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification)

        cleanup:
        stopBufferHandler()
    }

    def 'when buffer differs from HEAD revision after explicit update request it should report dirty lines'() {
        setup:
        def filePath = repoPath.resolve('file')
        touchFile(filePath, 'line 1\n')
        addAndCommitFile(filePath)

        when:
        createAndStartBufferHandler(filePath)
        waitForPatchUpdateNotification()
        touchFile(filePath, 'new line 1\n')
        requestPatchUpdate()
        waitForPatchUpdateNotification()
        def dirtyMarkPainterSpecification = getDirtyMarkPainterSpecificationForLine(0)

        then:
        matchesChangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification)

        cleanup:
        stopBufferHandler()
    }

    def 'when buffer does not differ from HEAD revision after commit it should not report dirty lines'() {
        setup:
        def filePath = repoPath.resolve('file')
        touchFile(filePath, 'line 1\n')
        addAndCommitFile(filePath)
        touchFile(filePath, 'new line 1\n')

        when:
        createAndStartBufferHandler(filePath)
        waitForPatchUpdateNotification()
        addAndCommitFile(filePath)
        waitForPatchUpdateNotification()
        def dirtyMarkPainterSpecification = getDirtyMarkPainterSpecificationForLine(0)

        then:
        matchesUnchangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification)

        cleanup:
        stopBufferHandler()
    }
}
