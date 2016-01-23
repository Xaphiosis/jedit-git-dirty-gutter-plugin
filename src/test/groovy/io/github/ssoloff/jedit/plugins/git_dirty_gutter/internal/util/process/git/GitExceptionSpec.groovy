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
package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git

import java.nio.file.Paths
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Subject(GitException)
@Title('Unit tests for GitException#getMessage')
class GitException_GetMessageSpec extends Specification {
    def 'when the message summary is not empty it should include the message summary'() {
        given: 'an exception with a non-empty message summary'
        def messageSummary = '123custom message321'
        def e = GitException.newBuilder().withMessageSummary(messageSummary).build()

        expect: 'the message to include the message summary'
        e.message.startsWith(messageSummary)
    }

    def 'when the message summary is empty it should include the default message summary'() {
        given: 'an exception with an empty message summary'
        def e = GitException.newBuilder().withMessageSummary('').build()

        expect: 'the message to include the default message summary'
        e.message.startsWith(GitException.defaultMessageSummary)
    }

    def 'when the message summary is not specified it should include the default message summary'() {
        given: 'an exception without a message summary'
        def e = GitException.newBuilder().build()

        expect: 'the message to include the default message summary'
        e.message.startsWith(GitException.defaultMessageSummary)
    }

    def 'when the working directory path is specified it should include the working directory path'() {
        given: 'an exception with a working directory path'
        def e = GitException.newBuilder().withWorkingDirPath(Paths.get('/path/to/working-dir')).build()

        expect: 'the message to include the working directory path'
        e.message =~ /(?m)^\s*working dir:/
    }

    def 'when the working directory path is not specified it should not include the working directory path'() {
        given: 'an exception without a working directory path'
        def e = GitException.newBuilder().build()

        expect: 'the message to not include the working directory path'
        !(e.message =~ /(?m)^\s*working dir:/)
    }

    def 'when the command is specified it should include the command'() {
        given: 'an exception with a command'
        def e = GitException.newBuilder().withCommand(command).build()

        expect: 'the message to include the command'
        e.message =~ /(?m)^\s*command:/

        where:
        command                 | _
        []                      | _
        ['git']                 | _
        ['git', 'arg1']         | _
        ['git', 'arg1', 'arg2'] | _
    }

    def 'when the command is not specified it should not include the command'() {
        given: 'an exception without a command'
        def e = GitException.newBuilder().build()

        expect: 'the message to not include the command'
        !(e.message =~ /(?m)^\s*command:/)
    }

    def 'when the exit code is specified it should include the exit code'() {
        given: 'an exception with an exit code'
        def e = GitException.newBuilder().withExitCode(0).build()

        expect: 'the message to include the exit code'
        e.message =~ /(?m)^\s*exit code:/
    }

    def 'when the exit code is not specified it should not include the exit code'() {
        given: 'an exception without an exit code'
        def e = GitException.newBuilder().build()

        expect: 'the message to not include the exit code'
        !(e.message =~ /(?m)^\s*exit code:/)
    }

    def 'when the standard output content is not empty it should include the standard output content'() {
        given: 'an exception with non-empty standard output content'
        def e = GitException.newBuilder().withOutput('foo').build()

        expect: 'the message to include the standard output content'
        e.message =~ /(?m)^\s*output:/
    }

    def 'when the standard output content is empty it should not include the standard output content'() {
        given: 'an exception with empty standard output content'
        def e = GitException.newBuilder().withOutput('').build()

        expect: 'the message to not include the standard output content'
        !(e.message =~ /(?m)^\s*output:/)
    }

    def 'when the standard output content is not specified it should not include the standard output content'() {
        given: 'an exception without standard output content'
        def e = GitException.newBuilder().build()

        expect: 'the message to not include the standard output content'
        !(e.message =~ /(?m)^\s*output:/)
    }

    def 'when the standard error content is not empty it should include the standard error content'() {
        given: 'an exception with non-empty standard error content'
        def e = GitException.newBuilder().withError('foo').build()

        expect: 'the message to include the standard error content'
        e.message =~ /(?m)^\s*error:/
    }

    def 'when the standard error content is empty it should not include the standard error content'() {
        given: 'an exception with empty standard error content'
        def e = GitException.newBuilder().withError('').build()

        expect: 'the message to not include the standard error content'
        !(e.message =~ /(?m)^\s*error:/)
    }

    def 'when the standard error content is not specified it should not include the standard error content'() {
        given: 'an exception without standard error content'
        def e = GitException.newBuilder().build()

        expect: 'the message to not include the standard error content'
        !(e.message =~ /(?m)^\s*error:/)
    }
}
