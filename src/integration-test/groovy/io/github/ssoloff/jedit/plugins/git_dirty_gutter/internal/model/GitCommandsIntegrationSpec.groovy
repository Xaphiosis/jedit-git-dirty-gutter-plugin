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
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitException
import spock.lang.Subject
import spock.lang.Title

class GitCommandsIntegrationSpecification extends GitIntegrationSpecification {
    protected gitCommands

    def setup() {
        gitCommands = new GitCommands(newGitRunner())
    }
}

@Subject(GitCommands)
@Title('Integration tests for GitCommands#getCommitRefAtHeadRevision')
class GitCommands_GetCommitRefAtHeadRevisionIntegrationSpec extends GitCommandsIntegrationSpecification {
    def 'when file exists on HEAD it should return commit ref'() {
        given: 'a file committed on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)

        when: 'getting the commit ref at the HEAD revision'
        def commitRef = gitCommands.getCommitRefAtHeadRevision(repoPath.relativize(filePath))

        then: 'it should be a SHA-1 hash'
        commitRef ==~ /[0-9a-f]{40}/
    }

    def 'when file is inside repo but does not exist on HEAD it should throw an exception'() {
        given: 'a file present inside the repo but not committed on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)

        when: 'getting the commit ref at the HEAD revision'
        gitCommands.getCommitRefAtHeadRevision(repoPath.relativize(filePath))

        then: 'it should throw an exception'
        thrown(GitException)
    }

    def 'when file is outside repo it should throw an exception'() {
        given: 'a file present outside the repo'
        def filePath = newTemporaryFile()
        def gitCommands = new GitCommands(newGitRunnerForRepo(filePath.parent))

        when: 'getting the commit ref at the HEAD revision'
        gitCommands.getCommitRefAtHeadRevision(filePath)

        then: 'it should throw an exception'
        thrown(GitException)
    }
}

@Subject(GitCommands)
@Title('Integration tests for GitCommands#getRepoRelativeFilePathAtHeadRevision')
class GitCommands_GetRepoRelativeFilePathAtHeadRevisionIntegrationSpec extends GitCommandsIntegrationSpecification {
    def 'when file exists on HEAD it should return repo-relative path'() {
        given: 'a file committed on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)

        when: 'getting the repo-relative path at the HEAD revision'
        def repoRelativeFilePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(filePath)

        then: 'it should be the repo-relative path of the file'
        repoRelativeFilePath == repoPath.relativize(filePath)
    }

    def 'when file is inside repo but does not exist on HEAD it should throw an exception'() {
        given: 'a file present inside the repo but not committed on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)

        when: 'getting the repo-relative path at the HEAD revision'
        gitCommands.getRepoRelativeFilePathAtHeadRevision(filePath)

        then: 'it should throw an exception'
        thrown(GitException)
    }

    def 'when file is outside repo it should throw an exception'() {
        given: 'a file present outside the repo'
        def filePath = newTemporaryFile()

        when: 'getting the repo-relative path at the HEAD revision'
        gitCommands.getRepoRelativeFilePathAtHeadRevision(filePath)

        then: 'it should throw an exception'
        thrown(GitException)
    }
}

@Subject(GitCommands)
@Title('Integration tests for GitCommands#isFilePresentAtHeadRevision')
class GitCommands_IsFilePresentAtHeadRevisionIntegrationSpec extends GitCommandsIntegrationSpecification {
    def 'when file exists on HEAD it should return true'() {
        given: 'a file committed on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)

        when: 'asking if the file is present at the HEAD revision'
        def result = gitCommands.isFilePresentAtHeadRevision(filePath)

        then: 'it should be true'
        result == true
    }

    def 'when file is inside repo but does not exist on HEAD it should return false'() {
        given: 'a file present inside the repo but not committed on HEAD'
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)

        when: 'asking if the file is present at the HEAD revision'
        def result = gitCommands.isFilePresentAtHeadRevision(filePath)

        then: 'it should be false'
        result == false
    }

    def 'when file is outside repo it should return false'() {
        given: 'a file present outside the repo'
        def filePath = newTemporaryFile()

        when: 'asking if the file is present at the HEAD revision'
        def result = gitCommands.isFilePresentAtHeadRevision(filePath)

        then: 'it should be false'
        result == false
    }
}

@Subject(GitCommands)
@Title('Integration tests for GitCommands#readFileContentAtHeadRevision')
class GitCommands_ReadFileContentAtHeadRevisionIntegrationSpec extends GitCommandsIntegrationSpecification {
    def 'when file exists on HEAD it should read file content'() {
        given: 'a file committed on HEAD'
        def filePath = repoPath.resolve('file')
        def fileContent = 'line1\nline2\n'
        touchFile(filePath, fileContent)
        addAndCommitFile(filePath)

        and: 'a writer to capture the file content'
        def writer = new StringWriter()

        when: 'reading the file content at the HEAD revision'
        gitCommands.readFileContentAtHeadRevision(repoPath.relativize(filePath), writer)

        then: 'the writer should contain the file content'
        writer.toString() == 'line1\nline2\n'
    }

    def 'when file does not exist on HEAD it should throw an exception'() {
        given: 'a file present inside the repo but not committed on HEAD'
        def filePath = repoPath.resolve('file')
        touchFile(filePath)

        when: 'reading the file content at the HEAD revision'
        gitCommands.readFileContentAtHeadRevision(repoPath.relativize(filePath), new StringWriter())

        then: 'it should throw an exception'
        thrown(GitException)
    }
}
