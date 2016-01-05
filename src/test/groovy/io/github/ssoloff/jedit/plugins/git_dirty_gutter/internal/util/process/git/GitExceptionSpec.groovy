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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git

import java.nio.file.Paths
import spock.lang.Specification

class GitExceptionSpec extends Specification {
    def 'getMessage - when the message summary is not empty it should include the message summary'() {
        setup:
        def messageSummary = '123custom message321'
        def e = GitException.newBuilder().withMessageSummary(messageSummary).build()

        expect:
        e.message.startsWith(messageSummary)
    }

    def 'getMessage - when the message summary is empty it should include the default message summary'() {
        setup:
        def e = GitException.newBuilder().withMessageSummary('').build()

        expect:
        e.message.startsWith(GitException.defaultMessageSummary)
    }

    def 'getMessage - when the message summary is not specified it should include the default message summary'() {
        setup:
        def e = GitException.newBuilder().build()

        expect:
        e.message.startsWith(GitException.defaultMessageSummary)
    }

    def 'getMessage - when the working directory path is specified it should include the working directory path'() {
        setup:
        def e = GitException.newBuilder().withWorkingDirPath(Paths.get('/path/to/working-dir')).build()

        expect:
        e.message =~ /(?m)^\s*working dir:/
    }

    def 'getMessage - when the working directory path is not specified it should not include the working directory path'() {
        setup:
        def e = GitException.newBuilder().build()

        expect:
        e.message !=~ /(?m)^\s*working dir:/
    }

    def 'getMessage - when the command is specified it should include the command'() {
        setup:
        def e = GitException.newBuilder().withCommand(command).build()

        expect:
        e.message =~ /(?m)^\s*command:/

        where:
        command << [[], ['git'], ['git', 'arg1'], ['git', 'arg1', 'arg2']]
    }

    def 'getMessage - when the command is not specified it should not include the command'() {
        setup:
        def e = GitException.newBuilder().build()

        expect:
        e.message !=~ /(?m)^\s*command:/
    }

    def 'getMessage - when the exit code is specified it should include the exit code'() {
        setup:
        def e = GitException.newBuilder().withExitCode(0).build()

        expect:
        e.message =~ /(?m)^\s*exit code:/
    }

    def 'getMessage - when the exit code is not specified it should not include the exit code'() {
        setup:
        def e = GitException.newBuilder().build()

        expect:
        e.message !=~ /(?m)^\s*exit code:/
    }

    def 'getMessage - when the standard output content is not empty it should include the standard output content'() {
        setup:
        def e = GitException.newBuilder().withOutput('foo').build()

        expect:
        e.message =~ /(?m)^\s*output:/
    }

    def 'getMessage - when the standard output content is empty it should not include the standard output content'() {
        setup:
        def e = GitException.newBuilder().withOutput('').build()

        expect:
        e.message !=~ /(?m)^\s*output:/
    }

    def 'getMessage - when the standard output content is not specified it should not include the standard output content'() {
        setup:
        def e = GitException.newBuilder().build()

        expect:
        e.message !=~ /(?m)^\s*output:/
    }

    def 'getMessage - when the standard error content is not empty it should include the standard error content'() {
        setup:
        def e = GitException.newBuilder().withError('foo').build()

        expect:
        e.message =~ /(?m)^\s*error:/
    }

    def 'getMessage - when the standard error content is empty it should not include the standard error content'() {
        setup:
        def e = GitException.newBuilder().withError('').build()

        expect:
        e.message !=~ /(?m)^\s*error:/
    }

    def 'getMessage - when the standard error content is not specified it should not include the standard error content'() {
        setup:
        def e = GitException.newBuilder().build()

        expect:
        e.message !=~ /(?m)^\s*error:/
    }
}
