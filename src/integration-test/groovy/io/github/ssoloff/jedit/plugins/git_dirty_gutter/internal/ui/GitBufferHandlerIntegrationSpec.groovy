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
    private static def ADDED_DIRTY_MARK_COLOR = Color.GREEN
    private static def CHANGED_DIRTY_MARK_COLOR = Color.ORANGE
    private static def REMOVED_DIRTY_MARK_COLOR = Color.RED

    private def bufferHandler
    private def bufferHandlerListenerEvent = new AutoResetEvent()
    private def bufferHandlerListener = { bufferHandlerListenerEvent.signal() }

    private void createAndStartBufferHandler(filePath) {
        SwingUtilities.invokeAndWait({
            bufferHandler = createBufferHandlerForFile(filePath)
            bufferHandler.addListener(bufferHandlerListener)
            bufferHandler.start()
        })
    }

    private def createBufferHandlerForFile(filePath) {
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

    private def getDirtyMarkPainterSpecificationForLine(lineIndex) {
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

    private void requestPatchUpdate() {
        SwingUtilities.invokeAndWait({
            bufferHandler.updatePatch()
        })
    }

    private void stopBufferHandler() {
        SwingUtilities.invokeAndWait({
            bufferHandler.stop()
            bufferHandler.removeListener(bufferHandlerListener)
        })
    }

    private void waitForPatchUpdateNotification() {
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
