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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process

import common.io.ProcessExecutor
import java.nio.file.Paths
import spock.lang.Specification

class ProcessRunnerSpec extends Specification {
    private command = ['cmd'] as String[]
    private processRunner = new ProcessRunner({ createStubProcessExecutor(it) } as IProcessExecutorFactory)
    private workingDirPath = Paths.get('workingDir')

    private static createDefaultWriter() {
        new StringWriter()
    }

    private static createFailingWriter() {
        new Writer() {
            void close() {
            }

            void flush() {
            }

            void write(char[] cbuf, int off, int len) {
                throw new IOException()
            }
        }
    }

    private static createStubProcessExecutor(command) {
        new ProcessExecutor(command) {
            private ProcessExecutor.LineVisitor visitor

            ProcessExecutor addVisitor(ProcessExecutor.LineVisitor visitor) {
                this.visitor = visitor
                super.addVisitor(visitor)
            }

            Process start() {
                null
            }

            int waitFor() {
                this.visitor.process('stdout line', false)
                this.visitor.process('stderr line', true)
                0
            }
        }
    }

    def 'when exception occurs while writing standard output content it should throw an exception'() {
        setup:
        def outWriter = createFailingWriter()
        def errWriter = createDefaultWriter()

        when:
        processRunner.run(outWriter, errWriter, workingDirPath, command)

        then:
        thrown(IOException)
    }

    def 'when exception occurs while writing standard error content it should throw an exception'() {
        setup:
        def outWriter = createDefaultWriter()
        def errWriter = createFailingWriter()

        when:
        processRunner.run(outWriter, errWriter, workingDirPath, command)

        then:
        thrown(IOException)
    }
}
