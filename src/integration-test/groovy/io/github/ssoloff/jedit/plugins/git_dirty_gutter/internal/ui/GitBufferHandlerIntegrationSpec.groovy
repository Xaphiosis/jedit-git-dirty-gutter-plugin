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

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.IBuffer
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.ILog
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.StringUtils
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.ProcessRunner
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitRunner
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunner
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunnerFactory
import java.awt.Color
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import spock.lang.Specification

class GitBufferHandlerIntegrationSpec extends Specification {
    private static def ADDED_DIRTY_MARK_COLOR = Color.GREEN
    private static def CHANGED_DIRTY_MARK_COLOR = Color.ORANGE
    private static def REMOVED_DIRTY_MARK_COLOR = Color.RED

    private def repoPath = createTempDirectory()
    private def gitRunnerFactory = createGitRunnerFactory()
    private def bufferHandler = null
    private def bufferHandlerListenerLatch = new CountDownLatch(1)

    private void addAndCommitFile(Path filePath) {
        runGit('add', filePath)
        runGit('commit', '-m', 'test commit')
    }

    private void createAndStartBufferHandler(Path filePath) {
        SwingUtilities.invokeAndWait({
            bufferHandler = createBufferHandlerForFile(filePath)
            bufferHandler.setListener({ bufferHandlerListenerLatch.countDown() })
            bufferHandler.start()
        })
    }

    private GitBufferHandler createBufferHandlerForFile(Path filePath) {
        def buffer = Stub(IBuffer) {
            getFilePath() >> filePath
            getLines() >> StringUtils.splitLinesWithExplicitFinalLine(new String(Files.readAllBytes(filePath)))
        }
        def dirtyMarkPainterSpecificationFactoryContext = Stub(IDirtyMarkPainterSpecificationFactoryContext) {
            getAddedDirtyMarkColor() >> ADDED_DIRTY_MARK_COLOR
            getChangedDirtyMarkColor() >> CHANGED_DIRTY_MARK_COLOR
            getRemovedDirtyMarkColor() >> REMOVED_DIRTY_MARK_COLOR
        }
        def log = Stub(ILog)
        def context = Stub(IGitBufferHandlerContext) {
            getBuffer() >> buffer
            getCommitMonitorPollTimeInMilliseconds() >> 1000
            getDirtyMarkPainterSpecificationFactoryContext() >> dirtyMarkPainterSpecificationFactoryContext
            getGitRunnerFactory() >> gitRunnerFactory
            getLog() >> log
        }
        new GitBufferHandler(context)
    }

    private IGitRunner createGitRunner() {
        createGitRunnerForRepo(repoPath)
    }

    private static IGitRunnerFactory createGitRunnerFactory() {
        new IGitRunnerFactory() {
            IGitRunner createGitRunner(Path workingDirPath) {
                new GitRunner(new ProcessRunner(), workingDirPath, Paths.get('git'))
            }
        }
    }

    private IGitRunner createGitRunnerForRepo(Path repoPath) {
        gitRunnerFactory.createGitRunner(repoPath)
    }

    private Path createTempDirectory() {
        Files.createTempDirectory(this.class.simpleName)
    }

    private Path createTempFile() {
        Files.createTempFile(this.class.simpleName, null)
    }

    private static void deleteDirectory(Path dirPath) {
        if ((dirPath != null) && !dirPath.deleteDir()) {
            System.err.println "failed to delete directory $dirPath"
        }
    }

    private static void deleteFile(Path filePath) {
        if ((filePath != null) && !Files.deleteIfExists(filePath)) {
            System.err.println "failed to delete file $filePath"
        }
    }

    private def getDirtyMarkPainterSpecificationForLine(lineIndex) {
        def dirtyMarkPainterSpecification = null
        SwingUtilities.invokeAndWait({
            dirtyMarkPainterSpecification = bufferHandler.getDirtyMarkPainterSpecificationForLine(lineIndex)
        })
        dirtyMarkPainterSpecification
    }

    private void initRepo() {
        runGit('init')

        // configure required user properties
        runGit('config', 'user.name', 'TestUser')
        runGit('config', 'user.email', 'TestEmail')

        // create an initial commit so HEAD is present
        def filePath = repoPath.resolve('README')
        touchFile(filePath)
        addAndCommitFile(filePath)
    }

    private void matchesChangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification) {
        assert dirtyMarkPainterSpecification.color == CHANGED_DIRTY_MARK_COLOR
    }

    private void matchesUnchangedDirtyMarkPainterSpecification(dirtyMarkPainterSpecification) {
        assert dirtyMarkPainterSpecification == DirtyMarkPainterSpecification.NULL
    }

    private void runGit(Object... args) {
        def gitRunner = createGitRunner()
        gitRunner.run(new StringWriter(), args.each { it.toString() } as String[] )
    }

    private void stopBufferHandler() {
        SwingUtilities.invokeAndWait({
            bufferHandler.stop()
        })
    }

    private static void touchFile(Path filePath, String fileContent='') {
        def parentPath = filePath.parent
        if (Files.notExists(parentPath)) {
            assert parentPath.toFile().mkdirs()
        }

        filePath.setText(fileContent)
    }

    private void waitForPatchUpdateNotification() {
        bufferHandlerListenerLatch.await(30, TimeUnit.SECONDS)
    }

    def setup() {
        initRepo()
    }

    def cleanup() {
        deleteDirectory(repoPath)
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
}
