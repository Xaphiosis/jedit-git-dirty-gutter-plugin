/*
 * Copyright (C) 2016 Steven Soloff
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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A collection of methods for working with strings and character sequences.
 */
public final class StringUtils {
    private StringUtils() {
    }

    /**
     * Joins the specified collection of lines into a string when the final line
     * is explicitly specified.
     *
     * <p>
     * Consider a stream of characters that represents a logical collection of
     * lines due to the presence of newlines within the stream, as well as being
     * terminated by a final newline. When the content of this stream is split
     * into a collection of lines, the final line may either be explicitly or
     * implicitly specified.
     * </p>
     *
     * <p>
     * When explicitly specified, the last element in the resulting collection
     * will be a blank line. This blank line represents the virtual line where
     * content may be added at the end of the stream.
     * </p>
     *
     * <p>
     * When implicitly specified, the last element in the resulting collection
     * will be the last line in the stream.
     * </p>
     *
     * <p>
     * Note that if the stream does not end with a final newline, there is no
     * difference between the explicit and implicit algorithms.
     * </p>
     *
     * @param lines
     *        The collection of lines to join.
     *
     * @return A string containing the joined lines.
     */
    public static String joinLinesWithExplicitFinalLine(final List<? extends CharSequence> lines) {
        return removeImplicitFinalLine(joinLinesWithImplicitFinalLine(lines));
    }

    /**
     * Joins the specified collection of lines into a string when the final line
     * is implicitly specified.
     *
     * <p>
     * See {@link #joinLinesWithExplicitFinalLine} for an explanation of an
     * implicitly-specified final line.
     * </p>
     *
     * @param lines
     *        The collection of lines to join.
     *
     * @return A string containing the joined lines.
     */
    public static String joinLinesWithImplicitFinalLine(final List<? extends CharSequence> lines) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence line : lines) {
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }

    private static List<String> removeExplicitFinalLine(final List<String> lines) {
        final int finalLineIndex = lines.size() - 1;
        if (finalLineIndex > 0) {
            final String finalLine = lines.get(finalLineIndex);
            if (finalLine.isEmpty()) {
                return lines.subList(0, finalLineIndex);
            }
        }
        return lines;
    }

    private static String removeImplicitFinalLine(final String str) {
        return str.isEmpty() ? str : str.substring(0, str.length() - 1);
    }

    /**
     * Splits the specified character sequence into a collection of lines with
     * an explicitly-specified final line.
     *
     * <p>
     * See {@link #joinLinesWithExplicitFinalLine} for an explanation of an
     * explicitly-specified final line.
     * </p>
     *
     * @param input
     *        The character sequence to split into lines.
     *
     * @return The collection of split lines.
     */
    public static List<String> splitLinesWithExplicitFinalLine(final CharSequence input) {
        if (input.length() == 0) {
            return Collections.emptyList();
        }

        final Pattern pattern = Pattern.compile("\r?\n|\r"); //$NON-NLS-1$
        return Arrays.asList(pattern.split(input, -1));
    }

    /**
     * Splits the specified character sequence into a collection of lines with
     * an implicitly-specified final line.
     *
     * <p>
     * See {@link #joinLinesWithExplicitFinalLine} for an explanation of an
     * explicitly-specified final line.
     * </p>
     *
     * @param input
     *        The character sequence to split into lines.
     *
     * @return The collection of split lines.
     */
    public static List<String> splitLinesWithImplicitFinalLine(final CharSequence input) {
        return removeExplicitFinalLine(splitLinesWithExplicitFinalLine(input));
    }
}
