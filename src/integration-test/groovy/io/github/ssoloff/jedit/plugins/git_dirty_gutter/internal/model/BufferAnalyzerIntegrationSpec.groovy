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

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.test.GitIntegrationSpecification
import java.util.concurrent.atomic.AtomicReference

class BufferAnalyzerIntegrationSpec extends GitIntegrationSpecification {
    private getCommitRefAtHeadRevision(repoRelativeFilePath) {
        def gitRunner = newGitRunner()
        def outWriter = new StringWriter()
        def result = gitRunner.run(outWriter, 'ls-tree', 'HEAD', repoRelativeFilePath.toString())
        assert result.exitCode == 0
        outWriter.toString().split(/\s+/)[2]
    }

    private newBufferAnalyzerForFile(filePath) {
        def buffer = newBufferForFile(filePath)
        def log = Stub(ILog)
        new BufferAnalyzer(buffer, newGitRunnerFactory(), log)
    }

    def 'createPatchBetweenHeadRevisionAndCurrentState - when file exists on HEAD it should return patch'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)
        touchFile(filePath, 'new content\n')
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

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
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

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
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

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
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

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
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

        when:
        def result = bufferAnalyzer.hasHeadRevisionChanged(commitRefRef)

        then:
        result == false
        commitRefRef.get() == null
    }
}
