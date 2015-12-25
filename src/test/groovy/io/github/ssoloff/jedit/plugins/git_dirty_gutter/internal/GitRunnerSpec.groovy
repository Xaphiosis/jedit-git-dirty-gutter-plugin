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

import java.nio.file.Path
import java.nio.file.Paths
import spock.lang.Specification

class GitRunnerSpec extends Specification {
    private def gitPath = Paths.get('git')
    private def workingDirPath = Paths.get('workingDir')

    private GitRunner createGitRunnerUnderTest(IProcessRunner processRunner) {
        new GitRunner(processRunner, gitPath, workingDirPath)
    }

    def 'it should pass configured working directory to process runner'() {
        setup:
        def processRunner = Mock(IProcessRunner)
        def gitRunner = createGitRunnerUnderTest(processRunner)

        when:
        gitRunner.run(new StringWriter())

        then:
        1 * processRunner.run(_, _, workingDirPath, _)
    }

    def 'it should pass configured git command line to process runner'() {
        setup:
        def processRunner = Mock(IProcessRunner)
        def gitRunner = createGitRunnerUnderTest(processRunner)

        when:
        gitRunner.run(new StringWriter(), 'arg1', 'arg2')

        then:
        1 * processRunner.run(_, _, _, [gitPath.toString(), 'arg1', 'arg2'])
    }

    def 'when the process exits without error it should capture stdout'() {
        setup:
        def processRunner = Stub(IProcessRunner)
        processRunner.run(_, _, _, _) >> { Writer outWriter, Writer errWriter, Path workingDirPath, String[] command ->
            outWriter.write('stdout-line-1\n')
            outWriter.write('stdout-line-2\n')
            0
        }
        def gitRunner = createGitRunnerUnderTest(processRunner)
        def outWriter = new StringWriter()

        when:
        gitRunner.run(outWriter)

        then:
        outWriter.toString() == 'stdout-line-1\nstdout-line-2\n'
    }

    def 'when the process exits with error it should throw an exception'() {
        setup:
        def processRunner = Stub(IProcessRunner)
        processRunner.run(_, _, _, _) >> { Writer outWriter, Writer errWriter, Path workingDirPath, String[] command ->
            errWriter.write('stderr-line-1\n')
            errWriter.write('stderr-line-2\n')
            1
        }
        def gitRunner = createGitRunnerUnderTest(processRunner)

        when:
        gitRunner.run(new StringWriter())

        then:
        def e = thrown(GitException)
        e.exitCode == 1
        e.message == 'stderr-line-1\nstderr-line-2\n'
    }
}
