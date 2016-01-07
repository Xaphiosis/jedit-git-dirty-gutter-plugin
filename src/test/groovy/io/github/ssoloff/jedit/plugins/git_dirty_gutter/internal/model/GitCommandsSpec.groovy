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

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitException
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitRunnerResult
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunner
import java.nio.file.Paths
import spock.lang.Specification

class GitCommandsSpecification extends Specification {
    protected static GitRunnerResult newGitRunnerResultWithExitCode(int exitCode) {
        new GitRunnerResult(Paths.get('workingDir'), ['git'], exitCode)
    }
}

class GitCommands_GetCommitRefAtHeadRevisionSpec extends GitCommandsSpecification {
    def 'when working directory is inside repo it should return commit ref'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('28573fea3903ca83e973ae9d05d5d32942d1589f\n')
                newGitRunnerResultWithExitCode(0)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def commitRef = gitCommands.getCommitRefAtHeadRevision(Paths.get('subdir/file'))

        then:
        commitRef == '28573fea3903ca83e973ae9d05d5d32942d1589f'
    }

    def 'when Git returns an unexpected exit code it should throw an exception'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('28573fea3903ca83e973ae9d05d5d32942d1589f\n')
                newGitRunnerResultWithExitCode(1)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.getCommitRefAtHeadRevision(Paths.get('subdir/file'))

        then:
        def e = thrown(GitException)
        e.exitCode != null
    }

    def 'when Git produces an unexpected output it should throw an exception'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('28573fea3903ca83e973ae9d05d5d32942d1589f\n')
                outWriter.write('a29cfccdb5beec24c9eca88cd0adecd0f165d7aa\n')
                newGitRunnerResultWithExitCode(0)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.getCommitRefAtHeadRevision(Paths.get('subdir/file'))

        then:
        def e = thrown(GitException)
        e.output != null
    }
}

class GitCommands_GetRepoRelativeFilePathAtHeadRevisionSpec extends GitCommandsSpecification {
    def 'when file exists on HEAD it should return repo-relative path'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir/file\n')
                newGitRunnerResultWithExitCode(0)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def repoRelativePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        repoRelativePath == Paths.get('subdir/file')
    }

    def 'when file is inside repo but does not exist on HEAD it should throw an exception'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> {
                // empty stdout
                newGitRunnerResultWithExitCode(0)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.getRepoRelativeFilePathAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        thrown(GitException)
    }

    def 'when Git returns an unexpected exit code it should throw an exception'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir/file\n')
                newGitRunnerResultWithExitCode(1)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.getRepoRelativeFilePathAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        def e = thrown(GitException)
        e.exitCode != null
    }

    def 'when Git produces an unexpected output it should throw an exception'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir/file\n')
                outWriter.write('subdir/another-file\n')
                newGitRunnerResultWithExitCode(0)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.getRepoRelativeFilePathAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        def e = thrown(GitException)
        e.output != null
    }
}

class GitCommands_IsFilePresentAtHeadRevisionSpec extends GitCommandsSpecification {
    def 'when file exists on HEAD it should return true'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir/file\n')
                newGitRunnerResultWithExitCode(0)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def result = gitCommands.isFilePresentAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        result == true
    }

    def 'when file is inside repo but does not exist on HEAD it should return false'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> {
                // empty stdout
                newGitRunnerResultWithExitCode(0)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def result = gitCommands.isFilePresentAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        result == false
    }

    def 'when Git returns a nonzero exit code it should return false'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir/file\n')
                newGitRunnerResultWithExitCode(1)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def result = gitCommands.isFilePresentAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        result == false
    }

    def 'when Git produces an unexpected output it should return false'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir/file\n')
                outWriter.write('subdir/another-file\n')
                newGitRunnerResultWithExitCode(0)
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def result = gitCommands.isFilePresentAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        result == false
    }

    def 'when Git produces an expected error it should return false'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> {
                throw GitException.newBuilder().withExitCode(128).build()
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def result = gitCommands.isFilePresentAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        result == false
    }

    def 'when Git produces an unexpected error it should return false'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> {
                throw GitException.newBuilder().withExitCode(129).build()
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.isFilePresentAtHeadRevision(Paths.get('/root/subdir/file'))

        then:
        thrown(GitException)
    }
}

class GitCommands_ReadFileContentAtHeadRevisionSpec extends GitCommandsSpecification {
    def 'when file exists on HEAD it should read file content'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('line1\n')
                outWriter.write('line2\n')
                newGitRunnerResultWithExitCode(0)
            }
        }
        def gitCommands = new GitCommands(gitRunner)
        def writer = new StringWriter()

        when:
        gitCommands.readFileContentAtHeadRevision(Paths.get('file'), writer)

        then:
        writer.toString() == 'line1\nline2\n'
    }

    def 'when Git returns an unexpected exit code it should throw an exception'() {
        given:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('line1\n')
                outWriter.write('line2\n')
                newGitRunnerResultWithExitCode(1)
            }
        }
        def gitCommands = new GitCommands(gitRunner)
        def writer = new StringWriter()

        when:
        gitCommands.readFileContentAtHeadRevision(Paths.get('file'), writer)

        then:
        def e = thrown(GitException)
        e.exitCode != null
    }
}
