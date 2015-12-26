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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal

import java.nio.file.Paths
import spock.lang.Specification

class GitCommandsSpec extends Specification {
    def 'diffFiles - when the files are the same it should produce an empty difference'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                // empty stdout
                0
            }
        }
        def gitCommands = new GitCommands(gitRunner)
        def writer = new StringWriter()

        when:
        def isDifferent = gitCommands.diffFiles(Paths.get('original-file'), Paths.get('new-file'), writer)

        then:
        !isDifferent
        writer.toString().isEmpty()
    }

    def 'diffFiles - when the files are different it should produce the difference between the two files'() {
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
        def isDifferent = gitCommands.diffFiles(Paths.get('original-file'), Paths.get('new-file'), writer)

        then:
        isDifferent
        writer.toString() == 'line1\nline2\n'
    }

    def 'diffFiles - when Git exits without error and when Git returns an unexpected exit code it should throw an exception'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                // empty stdout
                2
            }
        }
        def gitCommands = new GitCommands(gitRunner)
        def writer = new StringWriter()

        when:
        gitCommands.diffFiles(Paths.get('original-file'), Paths.get('new-file'), writer)

        then:
        thrown(RuntimeException)
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
        thrown(RuntimeException)
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
        thrown(RuntimeException)
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
        thrown(RuntimeException)
    }
}
