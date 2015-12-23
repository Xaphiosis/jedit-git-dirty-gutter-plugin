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

import common.io.ProcessExecutor
import spock.lang.Specification

class GetRepoRelativePathGitCommandSpec extends Specification {
    def executor = new ProcessExecutor('')
    def command = new GetRepoRelativePathGitCommand(executor)

    def 'when file exists on HEAD it should provide repo-relative path'() {
        setup:
        def repoRelativePath = 'subdir1/subdir2/file'

        when:
        def processResult = command.process(repoRelativePath, false)

        then:
        processResult == false
        command.getRepoRelativePath() == repoRelativePath
    }

    def 'when file does not exist on HEAD it should set repo-relative path to null'() {
        expect:
        // process() not called in this case because stdout and stderr are empty
        command.getRepoRelativePath() == null
    }

    def 'when file does not exist in repo it should set repo-relative path to null'() {
        when:
        def processResult = command.process('error message', true)

        then:
        processResult == true
        command.getRepoRelativePath() == null
    }
}
