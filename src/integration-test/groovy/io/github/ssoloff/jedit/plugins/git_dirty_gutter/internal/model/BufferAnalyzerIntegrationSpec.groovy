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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.StringUtils
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.ProcessRunner
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitRunner
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunner
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunnerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BufferAnalyzerIntegrationSpec extends Specification {
    @Rule
    private TemporaryFolder temporaryFolder = new TemporaryFolder()

    private def repoPath = null
    private def gitRunnerFactory = createGitRunnerFactory()

    private void addAndCommitFile(Path filePath) {
        runGit('add', filePath)
        runGit('commit', '-m', 'test commit')
    }

    private BufferAnalyzer createBufferAnalyzerForFile(Path filePath) {
        def buffer = new IBuffer() {
            Path getFilePath() {
                filePath
            }
            List<String> getLines() {
                StringUtils.splitLinesWithExplicitFinalLine(new String(Files.readAllBytes(filePath)))
            }
        }
        def log = Stub(ILog)
        new BufferAnalyzer(buffer, gitRunnerFactory, log)
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

    private String getCommitRefAtHeadRevision(Path repoRelativeFilePath) {
        def gitRunner = createGitRunner()
        def outWriter = new StringWriter()
        def result = gitRunner.run(outWriter, 'ls-tree', 'HEAD', repoRelativeFilePath.toString())
        assert result.exitCode == 0
        outWriter.toString().split(/\s+/)[2]
    }

    private void initRepo() {
        repoPath = temporaryFolder.newFolder().toPath()

        runGit('init')

        // configure required user properties
        runGit('config', 'user.name', 'TestUser')
        runGit('config', 'user.email', 'TestEmail')

        // create an initial commit so HEAD is present
        def filePath = repoPath.resolve('README')
        touchFile(filePath)
        addAndCommitFile(filePath)
    }

    private void runGit(Object... args) {
        def gitRunner = createGitRunner()
        gitRunner.run(new StringWriter(), args.each { it.toString() } as String[] )
    }

    private static void touchFile(Path filePath, String fileContent='') {
        def parentPath = filePath.parent
        if (Files.notExists(parentPath)) {
            assert parentPath.toFile().mkdirs()
        }

        filePath.setText(fileContent)
    }

    def setup() {
        initRepo()
    }

    def 'createPatchBetweenHeadRevisionAndCurrentState - when file exists on HEAD it should return patch'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)
        touchFile(filePath, 'new content\n')
        def bufferAnalyzer = createBufferAnalyzerForFile(filePath)

        when:
        def patch = bufferAnalyzer.createPatchBetweenHeadRevisionAndCurrentState()

        then:
        patch.deltas.size() == 1
    }

    def 'createPatchBetweenHeadRevisionAndCurrentState - when file does not exist on HEAD it should return an empty patch'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        // do not commit so it does not exist on HEAD
        def bufferAnalyzer = createBufferAnalyzerForFile(filePath)

        when:
        def patch = bufferAnalyzer.createPatchBetweenHeadRevisionAndCurrentState()

        then:
        patch.deltas.size() == 0
    }

    def 'hasHeadRevisionChanged - when HEAD revision has changed it should return true and update commit ref'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)
        def oldCommitRef = getCommitRefAtHeadRevision(repoPath.relativize(filePath))
        def commitRefRef = new AtomicReference<String>(oldCommitRef)
        touchFile(filePath, 'new content\n')
        addAndCommitFile(filePath)
        def newCommitRef = getCommitRefAtHeadRevision(repoPath.relativize(filePath))
        assert oldCommitRef != newCommitRef
        def bufferAnalyzer = createBufferAnalyzerForFile(filePath)

        when:
        def result = bufferAnalyzer.hasHeadRevisionChanged(commitRefRef)

        then:
        result == true
        commitRefRef.get() == newCommitRef
    }

    def 'hasHeadRevisionChanged - when HEAD revision has not changed it should return false and not update commit ref'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)
        def oldCommitRef = getCommitRefAtHeadRevision(repoPath.relativize(filePath))
        def commitRefRef = new AtomicReference<String>(oldCommitRef)
        def bufferAnalyzer = createBufferAnalyzerForFile(filePath)

        when:
        def result = bufferAnalyzer.hasHeadRevisionChanged(commitRefRef)

        then:
        result == false
        commitRefRef.get() == oldCommitRef
    }

    def 'hasHeadRevisionChanged - when file does not exist on HEAD it should return false and not update commit ref'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        // do not commit so it does not exist on HEAD
        def commitRefRef = new AtomicReference<String>(null)
        def bufferAnalyzer = createBufferAnalyzerForFile(filePath)

        when:
        def result = bufferAnalyzer.hasHeadRevisionChanged(commitRefRef)

        then:
        result == false
        commitRefRef.get() == null
    }
}
