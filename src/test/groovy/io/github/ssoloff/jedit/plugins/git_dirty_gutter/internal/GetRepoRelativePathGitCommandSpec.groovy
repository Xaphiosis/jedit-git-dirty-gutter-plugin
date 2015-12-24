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

class GetRepoRelativePathGitCommandSpec extends Specification {
    private def command = createCommandUnderTest()

    private static def createCommandUnderTest() {
        new GetRepoRelativePathGitCommand(Paths.get('/root/subdir1/subdir2/file'), Paths.get('git'))
    }

    def 'process - when stdout is not empty - when stderr is empty - should set repo-relative path'() {
        setup:
        def repoRelativePath = Paths.get('subdir1/subdir2/file')

        when:
        def processResult = command.process(repoRelativePath.toString(), false)

        then:
        processResult == false
        command.getRepoRelativePath() == repoRelativePath
    }

    def 'process - when stdout is empty - when stderr is empty - should not set repo-relative path'() {
        expect:
        // process not called in this case because stdout and stderr are empty
        command.getRepoRelativePath() == null
    }

    def 'process - when stdout is empty - when stderr is not empty - should not set repo-relative path'() {
        when:
        def processResult = command.process('error message', true)

        then:
        processResult == true
        command.getRepoRelativePath() == null
    }
}
