/*
 * Copyright (C) 2015-2016 Steven Soloff
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
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import spock.lang.Subject
import spock.lang.Title

class BufferAnalyzerIntegrationSpecification extends GitIntegrationSpecification {
    protected BufferAnalyzer newBufferAnalyzerForFile(Path filePath) {
        def buffer = newBufferForFile(filePath)
        def log = Stub(ILog)
        new BufferAnalyzer(buffer, newGitRunnerFactory(), log)
    }
}

@Subject(BufferAnalyzer)
@Title('Integration tests for BufferAnalyzer#createPatchBetweenHeadRevisionAndCurrentState')
class BufferAnalyzer_CreatePatchBetweenHeadRevisionAndCurrentStateIntegrationSpec
        extends BufferAnalyzerIntegrationSpecification {
    def 'when file exists on HEAD it should return patch'() {
        given: 'a file committed on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)

        and: 'the current file contents changed'
        touchFile(filePath, 'new content\n')

        and: 'a buffer analyzer for the file'
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

        when: 'creating a patch between the HEAD revision and the current state'
        def patch = bufferAnalyzer.createPatchBetweenHeadRevisionAndCurrentState()

        then: 'the patch should not be empty'
        patch.deltas.size() == 1
    }

    def 'when file does not exist on HEAD it should return an empty patch'() {
        given: 'a file not present on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)

        and: 'a buffer analyzer for the file'
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

        when: 'creating a patch between the HEAD revision and the current state'
        def patch = bufferAnalyzer.createPatchBetweenHeadRevisionAndCurrentState()

        then: 'the patch should be empty'
        patch.deltas.size() == 0
    }
}

@Subject(BufferAnalyzer)
@Title('Integration tests for BufferAnalyzer#hasHeadRevisionChanged')
class BufferAnalyzer_HasHeadRevisionChangedIntegrationSpec extends BufferAnalyzerIntegrationSpecification {
    private getCommitRefAtHeadRevision(repoRelativeFilePath) {
        def gitRunner = newGitRunner()
        def outWriter = new StringWriter()
        def result = gitRunner.run(outWriter, 'ls-tree', 'HEAD', repoRelativeFilePath.toString())
        assert result.exitCode == 0
        outWriter.toString().split(/\s+/)[2]
    }

    def 'when HEAD revision has changed it should return true and update commit ref'() {
        given: 'a file committed on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)

        and: 'the commit ref for the file at this commit'
        def oldCommitRef = getCommitRefAtHeadRevision(repoPath.relativize(filePath))

        and: 'modifying and committing the same file'
        touchFile(filePath, 'new content\n')
        addAndCommitFile(filePath)

        and: 'the new commit ref for the file at this commit'
        def newCommitRef = getCommitRefAtHeadRevision(repoPath.relativize(filePath))
        assert oldCommitRef != newCommitRef

        and: 'a buffer analyzer for the file'
        def commitRefRef = new AtomicReference<String>(oldCommitRef)
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

        when: 'asking if the HEAD revision has changed'
        def result = bufferAnalyzer.hasHeadRevisionChanged(commitRefRef)

        then: 'it should be true'
        result == true

        and: 'the latest commit ref should be the new commit ref'
        commitRefRef.get() == newCommitRef
    }

    def 'when HEAD revision has not changed it should return false and not update commit ref'() {
        given: 'a file committed on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)

        and: 'the commit ref for the file at this commit'
        def oldCommitRef = getCommitRefAtHeadRevision(repoPath.relativize(filePath))

        and: 'a buffer analyzer for the file'
        def commitRefRef = new AtomicReference<String>(oldCommitRef)
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

        when: 'asking if the HEAD revision has changed'
        def result = bufferAnalyzer.hasHeadRevisionChanged(commitRefRef)

        then: 'it should be false'
        result == false

        and: 'the latest commit ref should be the old commit ref'
        commitRefRef.get() == oldCommitRef
    }

    def 'when file does not exist on HEAD it should return false and not update commit ref'() {
        given: 'a file that does not exist on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)

        and: 'a buffer analyzer for this file'
        def commitRefRef = new AtomicReference<String>(null)
        def bufferAnalyzer = newBufferAnalyzerForFile(filePath)

        when: 'asking if the HEAD revision has changed'
        def result = bufferAnalyzer.hasHeadRevisionChanged(commitRefRef)

        then: 'it should be false'
        result == false

        and: 'the latest commit ref should be null'
        commitRefRef.get() == null
    }
}
