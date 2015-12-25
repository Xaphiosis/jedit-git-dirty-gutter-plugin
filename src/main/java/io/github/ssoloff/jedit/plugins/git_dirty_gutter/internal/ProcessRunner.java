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

/**
 * Implementation of {@link IProcessRunner} that uses the jEdit common controls
 * plugin's {@code ProcessExecutor} to run the process.
 */
final class ProcessRunner implements IProcessRunner {
    /*
     * @see io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.IProcessRunner#run(java.io.Writer, java.io.Writer, java.nio.file.Path, java.lang.String[])
     */
    @Override
    public int run(final Writer outWriter, final Writer errWriter, final Path workingDirPath, final String... command)
            throws IOException, InterruptedException {
        final ProcessExecutor processExecutor = new ProcessExecutor(command);
        processExecutor.setDirectory(workingDirPath.toString());
        processExecutor.addVisitor(new ProcessExecutor.LineVisitor() {
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
                } catch (@SuppressWarnings("unused") final IOException e) {
                    // TODO: rethrow exception outside of visitor
                    return false;
                }

                return true;
            }
        });
        final Process process = processExecutor.start();
        return process.waitFor();
    }
}
