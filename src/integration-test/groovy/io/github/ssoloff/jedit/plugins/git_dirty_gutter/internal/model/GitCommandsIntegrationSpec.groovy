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

class GitCommandsIntegrationSpecification extends GitIntegrationSpecification {
    protected gitCommands

    def setup() {
        gitCommands = new GitCommands(newGitRunner())
    }
}

class GitCommands_GetCommitRefAtHeadRevisionIntegrationSpec extends GitCommandsIntegrationSpecification {
    def 'when file exists on HEAD it should return commit ref'() {
        given:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)

        when:
        def commitRef = gitCommands.getCommitRefAtHeadRevision(repoPath.relativize(filePath))

        then:
        commitRef ==~ /[0-9a-f]{40}/
    }

    def 'when file is inside repo but does not exist on HEAD it should throw an exception'() {
        given:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        // do not commit so it does not exist on HEAD

        when:
        gitCommands.getCommitRefAtHeadRevision(repoPath.relativize(filePath))

        then:
        thrown(GitException)
    }

    def 'when file is outside repo it should throw an exception'() {
        given:
        def filePath = newTemporaryFile()
        def gitCommands = new GitCommands(newGitRunnerForRepo(filePath.parent))

        when:
        gitCommands.getCommitRefAtHeadRevision(filePath)

        then:
        thrown(GitException)
    }
}

class GitCommands_GetRepoRelativeFilePathAtHeadRevisionIntegrationSpec extends GitCommandsIntegrationSpecification {
    def 'when file exists on HEAD it should return repo-relative path'() {
        given:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)

        when:
        def repoRelativeFilePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(filePath)

        then:
        repoRelativeFilePath == repoPath.relativize(filePath)
    }

    def 'when file is inside repo but does not exist on HEAD it should throw an exception'() {
        given:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        // do not commit so it does not exist on HEAD

        when:
        gitCommands.getRepoRelativeFilePathAtHeadRevision(filePath)

        then:
        thrown(GitException)
    }

    def 'when file is outside repo it should throw an exception'() {
        given:
        def filePath = newTemporaryFile()

        when:
        gitCommands.getRepoRelativeFilePathAtHeadRevision(filePath)

        then:
        thrown(GitException)
    }
}

class GitCommands_IsFilePresentAtHeadRevisionIntegrationSpec extends GitCommandsIntegrationSpecification {
    def 'when file exists on HEAD it should return true'() {
        given:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        addAndCommitFile(filePath)

        when:
        def result = gitCommands.isFilePresentAtHeadRevision(filePath)

        then:
        result == true
    }

    def 'when file is inside repo but does not exist on HEAD it should return false'() {
        given:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        touchFile(filePath)
        // do not commit so it does not exist on HEAD

        when:
        def result = gitCommands.isFilePresentAtHeadRevision(filePath)

        then:
        result == false
    }

    def 'when file is outside repo it should return false'() {
        given:
        def filePath = newTemporaryFile()

        when:
        def result = gitCommands.isFilePresentAtHeadRevision(filePath)

        then:
        result == false
    }
}

class GitCommands_ReadFileContentAtHeadRevisionIntegrationSpec extends GitCommandsIntegrationSpecification {
    def 'when file exists on HEAD it should read file content'() {
        given:
        def filePath = repoPath.resolve('file')
        def fileContent = 'line1\nline2\n'
        touchFile(filePath, fileContent)
        addAndCommitFile(filePath)
        def writer = new StringWriter()

        when:
        gitCommands.readFileContentAtHeadRevision(repoPath.relativize(filePath), writer)

        then:
        writer.toString() == 'line1\nline2\n'
    }

    def 'when file does not exist on HEAD it should throw an exception'() {
        given:
        def filePath = repoPath.resolve('file')
        touchFile(filePath)
        // do not commit so it does not exist on HEAD

        when:
        gitCommands.readFileContentAtHeadRevision(repoPath.relativize(filePath), new StringWriter())

        then:
        thrown(GitException)
    }
}
