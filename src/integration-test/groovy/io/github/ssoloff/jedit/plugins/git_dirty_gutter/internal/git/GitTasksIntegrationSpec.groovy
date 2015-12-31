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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.git

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.IBuffer
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.ILog
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.ProcessRunner
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import spock.lang.Specification

class GitTasksIntegrationSpec extends Specification {
    private def repoPath = createTempDirectory()
    private def gitRunnerFactory = createGitRunnerFactory()
    private def gitTasks = createGitTasks()

    private void addAndCommitFile(Path filePath) {
        runGit('add', filePath)
        runGit('commit', '-m', 'test commit')
    }

    private IBuffer createBufferForFile(Path filePath) {
        Mock(IBuffer) {
            getFilePath() >> filePath
            getLines() >> Files.readAllLines(filePath, Charset.defaultCharset())
        }
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

    private GitTasks createGitTasks() {
        new GitTasks(gitRunnerFactory, Mock(ILog))
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

    private String getCommitRefAtHeadRevision(Path repoRelativeFilePath) {
        def gitRunner = createGitRunner()
        def outWriter = new StringWriter()
        assert 0 == gitRunner.run(outWriter, 'ls-tree', 'HEAD', repoRelativeFilePath.toString())
        outWriter.toString().split(/\s+/)[2]
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

    def cleanup() {
        deleteDirectory(repoPath)
    }

    def 'createPatchBetweenHeadRevisionAndCurrentState - when file exists on HEAD it should return patch'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)
        touchFile(filePath, 'new content\n')

        when:
        def patch = gitTasks.createPatchBetweenHeadRevisionAndCurrentState(createBufferForFile(filePath))

        then:
        patch != null
        patch.deltas.size() == 1
    }

    def 'createPatchBetweenHeadRevisionAndCurrentState - when file does not exist on HEAD it should return null'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        // do not commit so it does not exist on HEAD

        when:
        def patch = gitTasks.createPatchBetweenHeadRevisionAndCurrentState(createBufferForFile(filePath))

        then:
        patch == null
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

        when:
        def result = gitTasks.hasHeadRevisionChanged(createBufferForFile(filePath), commitRefRef)

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

        when:
        def result = gitTasks.hasHeadRevisionChanged(createBufferForFile(filePath), commitRefRef)

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

        when:
        def result = gitTasks.hasHeadRevisionChanged(createBufferForFile(filePath), commitRefRef)

        then:
        result == false
        commitRefRef.get() == null
    }
}
