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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model;

import java.util.List;

/**
 * A collection of methods that enhance the classes offered by the
 * {@code difflib} package.
 */
final class DiffLibUtils {
    private DiffLibUtils() {
    }

    /**
     * Methods that enhance the functionality of the {@link difflib.Chunk} class
     * for instances that represent the addition or change of content.
     */
    static final class AddOrChangeChunk {
        private AddOrChangeChunk() {
        }

        /**
         * Indicates the specified add or change chunk includes the specified
         * line.
         *
         * @param chunk
         *        The add or change chunk to examine.
         * @param lineIndex
         *        The zero-based index of the line to test for membership in the
         *        specified chunk; must not be negative.
         *
         * @return {@code true} if the specified add or change chunk includes
         *         the specified line; otherwise {@code false}.
         */
        static boolean isLineIncluded(final difflib.Chunk chunk, final int lineIndex) {
            assert lineIndex >= 0;

            final int chunkFirstLineIndex = chunk.getPosition();
            final int chunkLastLineIndex = (chunkFirstLineIndex + chunk.getLines().size()) - 1;
            return (lineIndex >= chunkFirstLineIndex) && (lineIndex <= chunkLastLineIndex);
        }
    }

    /**
     * Methods that enhance the functionality of the {@link difflib.Delta}
     * class.
     */
    static final class Delta {
        private Delta() {
        }

        /**
         * Indicates the specified delta represents added content.
         *
         * @param delta
         *        The delta to examine.
         *
         * @return {@code true} if the specified delta represents added content;
         *         otherwise {@code false}.
         */
        static boolean isContentAdded(final difflib.Delta delta) {
            return delta.getOriginal().getLines().isEmpty() && !delta.getRevised().getLines().isEmpty();
        }

        /**
         * Indicates the specified delta represents changed content.
         *
         * @param delta
         *        The delta to examine.
         *
         * @return {@code true} if the specified delta represents changed
         *         content; otherwise {@code false}.
         */
        static boolean isContentChanged(final difflib.Delta delta) {
            return !delta.getOriginal().getLines().isEmpty() && !delta.getRevised().getLines().isEmpty();
        }

        /**
         * Indicates the specified delta represents removed content.
         *
         * @param delta
         *        The delta to examine.
         *
         * @return {@code true} if the specified delta represents removed
         *         content; otherwise {@code false}.
         */
        static boolean isContentRemoved(final difflib.Delta delta) {
            return !delta.getOriginal().getLines().isEmpty() && delta.getRevised().getLines().isEmpty();
        }

        /**
         * Indicates the specified delta contains at least one context line.
         *
         * @param delta
         *        The delta to examine for context lines.
         *
         * @return {@code true} if the specified delta contains at least one
         *         context line; otherwise {@code false}.
         */
        static boolean isContextLinePresent(final difflib.Delta delta) {
            final List<?> originalChunkLines = delta.getOriginal().getLines();
            final List<?> revisedChunkLines = delta.getRevised().getLines();
            if (originalChunkLines.isEmpty() || revisedChunkLines.isEmpty()) {
                return false;
            }

            return originalChunkLines.get(0).equals(revisedChunkLines.get(0));
        }
    }

    /**
     * Methods that enhance the functionality of the {@link difflib.Patch}
     * class.
     */
    static final class Patch {
        private Patch() {
        }

        /**
         * Indicates the specified patch contains at least one context line.
         *
         * @param patch
         *        The patch to examine for context lines.
         *
         * @return {@code true} if the specified patch contains at least one
         *         context line; otherwise {@code false}.
         */
        static boolean isContextLinePresent(final difflib.Patch patch) {
            for (final difflib.Delta delta : patch.getDeltas()) {
                assert delta != null;
                if (Delta.isContextLinePresent(delta)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Methods that enhance the functionality of the {@link difflib.Chunk} class
     * for instances that represent the removal of content.
     */
    static final class RemoveChunk {
        private RemoveChunk() {
        }

        /**
         * Indicates the specified remove chunk includes the specified line.
         *
         * @param chunk
         *        The remove chunk to examine.
         * @param lineIndex
         *        The zero-based index of the line to test for membership in the
         *        specified chunk; must not be negative.
         *
         * @return {@code true} if the specified remove chunk includes the
         *         specified line; otherwise {@code false}.
         */
        static boolean isLineIncluded(final difflib.Chunk chunk, final int lineIndex) {
            assert lineIndex >= 0;

            final int chunkFirstLineIndex = chunk.getPosition();
            return (lineIndex == chunkFirstLineIndex) && chunk.getLines().isEmpty();
        }
    }

    /**
     * Methods that enhance the functionality of the {@link difflib.Delta} class
     * for instances that represent the removal of content.
     */
    static final class RemoveDelta {
        private RemoveDelta() {
        }

        /**
         * Indicates the specified remove delta is associated with content that
         * previously appeared before the first line.
         *
         * @param delta
         *        The delta to examine; must represent a removed delta.
         *
         * @return {@code true} if the specified remove delta is associated with
         *         content that previously appeared before the first line;
         *         otherwise {@code false}.
         */
        static boolean isBeforeFirstLine(final difflib.Delta delta) {
            assert Delta.isContentRemoved(delta);

            // NB: Unified diff handles this case by setting the one-based start
            // line number of the destination hunk to zero.  However, the
            // implementation of difflib.DiffUtils.parseUnifiedDiff() aliases this
            // virtual line number with the actual first line by not allowing the
            // zero-based line number to be negative.  We work around that issue
            // by examining the source hunk start line number to see if it includes
            // the first line of content.
            return delta.getOriginal().getPosition() == 0;
        }
    }
}
