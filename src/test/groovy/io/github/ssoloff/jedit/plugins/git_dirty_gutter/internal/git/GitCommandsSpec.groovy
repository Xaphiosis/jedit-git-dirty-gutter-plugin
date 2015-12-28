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

import java.nio.file.Paths
import spock.lang.Specification

class GitCommandsSpec extends Specification {
    def 'getCommitRefAtHeadRevision - when working directory is inside repo it should return commit ref'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('28573fea3903ca83e973ae9d05d5d32942d1589f\n')
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def commitRef = gitCommands.getCommitRefAtHeadRevision(Paths.get('subdir1/file'))

        then:
        commitRef == '28573fea3903ca83e973ae9d05d5d32942d1589f'
    }

    def 'getCommitRefAtHeadRevision - when Git returns an unexpected exit code it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('28573fea3903ca83e973ae9d05d5d32942d1589f\n')
                1
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.getCommitRefAtHeadRevision(Paths.get('subdir1/file'))

        then:
        def e = thrown(GitException)
        e.exitCode != null
    }

    def 'getCommitRefAtHeadRevision - when Git produces an unexpected output it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('28573fea3903ca83e973ae9d05d5d32942d1589f\n')
                outWriter.write('a29cfccdb5beec24c9eca88cd0adecd0f165d7aa\n')
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.getCommitRefAtHeadRevision(Paths.get('subdir1/file'))

        then:
        def e = thrown(GitException)
        e.output != null
    }

    def 'getRepoRelativeFilePathAtHeadRevision - when file exists on HEAD it should return repo-relative path'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir1/subdir2/file\n')
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def repoRelativePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(Paths.get('/root/subdir1/subdir2/file'))

        then:
        repoRelativePath == Paths.get('subdir1/subdir2/file')
    }

    def 'getRepoRelativeFilePathAtHeadRevision - when file is inside repo but does not exist on HEAD it should return null'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> {
                // empty stdout
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def repoRelativePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(Paths.get('/root/subdir1/subdir2/file'))

        then:
        repoRelativePath == null
    }

    def 'getRepoRelativeFilePathAtHeadRevision - when Git returns an unexpected exit code it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir1/subdir2/file\n')
                1
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.getRepoRelativeFilePathAtHeadRevision(Paths.get('/root/subdir1/subdir2/file'))

        then:
        def e = thrown(GitException)
        e.exitCode != null
    }

    def 'getRepoRelativeFilePathAtHeadRevision - when Git produces an unexpected output it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir1/subdir2/file\n')
                outWriter.write('subdir1/subdir2/another-file\n')
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.getRepoRelativeFilePathAtHeadRevision(Paths.get('/root/subdir1/subdir2/file'))

        then:
        def e = thrown(GitException)
        e.output != null
    }

    def 'isInsideRepo - when working directory is inside repo work tree it should return true'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('true\n')
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        expect:
        gitCommands.isInsideRepo() == true
    }

    def 'isInsideRepo - when working directory is inside repo but outside work tree it should return false'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('false\n')
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        expect:
        gitCommands.isInsideRepo() == false
    }

    def 'isInsideRepo - when working directory is outside repo it should return false'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> {
                // nonempty stderr
                throw GitException.newBuilder().withExitCode(128).build()
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        expect:
        gitCommands.isInsideRepo() == false
    }

    def 'isInsideRepo - when Git returns an unexpected exit code it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('true\n')
                1
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.isInsideRepo()

        then:
        def e = thrown(GitException)
        e.exitCode != null
    }

    def 'isInsideRepo - when Git produces an unexpected output with an unknown status it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('other\n')
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.isInsideRepo()

        then:
        def e = thrown(GitException)
        e.output != null
    }

    def 'isInsideRepo - when Git produces an unexpected output with additional lines it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('true\n')
                outWriter.write('true\n')
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.isInsideRepo()

        then:
        def e = thrown(GitException)
        e.output != null
    }

    def 'isInsideRepo - when Git runner throws an unexpected exception it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> {
                // nonempty stderr
                throw GitException.newBuilder().withExitCode(129).build()
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        gitCommands.isInsideRepo()

        then:
        thrown(GitException)
    }

    def 'readFileContentAtHeadRevision - when file exists on HEAD it should read file content'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('line1\n')
                outWriter.write('line2\n')
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)
        def writer = new StringWriter()

        when:
        gitCommands.readFileContentAtHeadRevision(Paths.get('file'), writer)

        then:
        writer.toString() == 'line1\nline2\n'
    }

    def 'readFileContentAtHeadRevision - when Git returns an unexpected exit code it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('line1\n')
                outWriter.write('line2\n')
                1
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
