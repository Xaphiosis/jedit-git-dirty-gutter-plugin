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

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.IProcessRunner
import java.nio.file.Path
import java.nio.file.Paths
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Subject(GitRunner)
@Title('Unit tests for GitRunner')
class GitRunnerSpec extends Specification {
    private static final PROGRAM_PATH = Paths.get('git')
    private static final WORKING_DIR_PATH = Paths.get('workingDir')

    private newGitRunner(processRunner) {
        new GitRunner(processRunner, WORKING_DIR_PATH, PROGRAM_PATH)
    }

    def 'it should pass configured working directory to process runner'() {
        given: 'a Git runner with a working directory path'
        def processRunner = Mock(IProcessRunner)
        def gitRunner = newGitRunner(processRunner)

        when: 'Git is run'
        gitRunner.run(new StringWriter())

        then: 'the process runner should be passed the configured working directory path'
        1 * processRunner.run(_, _, WORKING_DIR_PATH, _)
    }

    def 'it should pass configured program command line to process runner'() {
        given: 'a Git runner with a program path'
        def processRunner = Mock(IProcessRunner)
        def gitRunner = newGitRunner(processRunner)

        when: 'Git is run'
        gitRunner.run(new StringWriter(), 'arg1', 'arg2')

        then: 'the process runner should be passed the configured program path followed by the program arguments'
        1 * processRunner.run(_, _, _, [PROGRAM_PATH.toString(), 'arg1', 'arg2'])
    }

    def 'when the process exits without error and when the exit code is zero it should capture stdout'() {
        given: 'a process that exits with code 0 and writes to standard output'
        def processRunner = Stub(IProcessRunner)
        processRunner.run(_, _, _, _) >> { Writer outWriter, Writer errWriter, Path workingDirPath, String[] command ->
            outWriter.write('stdout-line-1\n')
            outWriter.write('stdout-line-2\n')
            0
        }
        def gitRunner = newGitRunner(processRunner)
        def outWriter = new StringWriter()

        when: 'Git is run'
        def result = gitRunner.run(outWriter)

        then: 'the result should capture the process context'
        with(result) {
            command == [PROGRAM_PATH.toString()]
            exitCode == 0
            workingDirPath == WORKING_DIR_PATH
        }

        and: 'it should capture standard output'
        outWriter.toString() == 'stdout-line-1\nstdout-line-2\n'
    }

    def 'when the process exits without error and when the exit code is nonzero it should capture stdout'() {
        given: 'a process that exits with code 1 and writes to standard output'
        def processRunner = Stub(IProcessRunner)
        processRunner.run(_, _, _, _) >> { Writer outWriter, Writer errWriter, Path workingDirPath, String[] command ->
            outWriter.write('stdout-line-1\n')
            outWriter.write('stdout-line-2\n')
            1
        }
        def gitRunner = newGitRunner(processRunner)
        def outWriter = new StringWriter()

        when: 'Git is run'
        def result = gitRunner.run(outWriter)

        then: 'the result should capture the process context'
        with(result) {
            command == [PROGRAM_PATH.toString()]
            exitCode == 1
            workingDirPath == WORKING_DIR_PATH
        }

        and: 'it should capture standard output'
        outWriter.toString() == 'stdout-line-1\nstdout-line-2\n'
    }

    def 'when the process exits with error it should throw an exception'() {
        given: 'a process that writes to standard error'
        def processRunner = Stub(IProcessRunner)
        processRunner.run(_, _, _, _) >> { Writer outWriter, Writer errWriter, Path workingDirPath, String[] command ->
            errWriter.write('stderr-line-1\n')
            errWriter.write('stderr-line-2\n')
            1
        }
        def gitRunner = newGitRunner(processRunner)

        when: 'Git is run'
        gitRunner.run(new StringWriter(), 'arg1', 'arg2')

        then: 'it should throw an exception containing the process context and captured standard error'
        def e = thrown(GitException)
        with(e) {
            command == [PROGRAM_PATH.toString(), 'arg1', 'arg2']
            error == 'stderr-line-1\nstderr-line-2\n'
            exitCode == 1
            workingDirPath == WORKING_DIR_PATH
        }
    }
}
