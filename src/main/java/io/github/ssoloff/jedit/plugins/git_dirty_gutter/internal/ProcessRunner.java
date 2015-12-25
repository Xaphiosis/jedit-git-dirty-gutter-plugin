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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal;

import common.io.ProcessExecutor;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Implementation of {@link IProcessRunner} that uses the jEdit common controls
 * plugin's {@code ProcessExecutor} to run the process.
 */
final class ProcessRunner implements IProcessRunner {
    private final IProcessExecutorFactory processExecutorFactory;

    /**
     * Initializes a new instance of the {@code ProcessRunner} class.
     */
    ProcessRunner() {
        this(new IProcessExecutorFactory() {
            @Override
            public ProcessExecutor createProcessExecutor(final String... command) {
                return new ProcessExecutor(command);
            }
        });
    }

    /**
     * Initializes a new instance of the {@code ProcessRunner} class using the
     * specified process executor factory.
     *
     * @param processExecutorFactory
     *        The factory used to create process executor instances.
     */
    ProcessRunner(final IProcessExecutorFactory processExecutorFactory) {
        this.processExecutorFactory = processExecutorFactory;
    }

    /*
     * @see io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.IProcessRunner#run(java.io.Writer, java.io.Writer, java.nio.file.Path, java.lang.String[])
     */
    @Override
    public int run(final Writer outWriter, final Writer errWriter, final Path workingDirPath, final String... command)
            throws IOException, InterruptedException {
        final ProcessExecutor processExecutor = processExecutorFactory.createProcessExecutor(command);
        processExecutor.setDirectory(workingDirPath.toString());
        final LineVisitor visitor = new LineVisitor(outWriter, errWriter);
        processExecutor.addVisitor(visitor);

        processExecutor.start();
        final int exitCode = processExecutor.waitFor();
        if (visitor.exception != null) {
            throw visitor.exception;
        }
        return exitCode;
    }

    private static final class LineVisitor implements ProcessExecutor.LineVisitor {
        private final Writer errWriter;
        private final Writer outWriter;

        @Nullable
        IOException exception = null;

        LineVisitor(final Writer outWriter, final Writer errWriter) {
            this.errWriter = errWriter;
            this.outWriter = outWriter;
        }

        @Override
        public boolean process(final String line, final boolean isError) {
            try {
                if (isError) {
                    errWriter.write(line);
                    errWriter.write("\n"); //$NON-NLS-1$
                } else {
                    outWriter.write(line);
                    outWriter.write("\n"); //$NON-NLS-1$
                }
            } catch (final IOException e) {
                exception = e;
                return false;
            }

            return true;
        }
    }
}
