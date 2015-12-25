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
    def 'getRepoRelativeFilePathAtHeadRevision - when file exists on HEAD it should return repo-relative path'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('subdir1/subdir2/file\n')
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
            }
        }
        def gitCommands = new GitCommands(gitRunner)

        when:
        def repoRelativePath = gitCommands.getRepoRelativeFilePathAtHeadRevision(Paths.get('/root/subdir1/subdir2/file'))

        then:
        repoRelativePath == null
    }

    def 'readFileContentAtHeadRevision - when file exists on HEAD it should read file content'() {
        setup:
        def gitRunner = Stub(IGitRunner) {
            run(_, _) >> { Writer outWriter, String[] args ->
                outWriter.write('line1\n')
                outWriter.write('line2\n')
            }
        }
        def gitCommands = new GitCommands(gitRunner)
        def writer = new StringWriter()

        when:
        gitCommands.readFileContentAtHeadRevision(Paths.get('file'), writer)

        then:
        writer.toString() == 'line1\nline2\n'
    }
}
