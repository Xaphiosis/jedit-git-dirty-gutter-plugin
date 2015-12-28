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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model;

import difflib.Chunk;
import difflib.Delta;
import difflib.Patch;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Provides various types of analysis for a patch.
 *
 * <p>
 * <b>NOTE:</b> The current implementation only handles patches that have been
 * produced with <i>zero</i> context lines. Attempting to use a patch with
 * <i>any</i> context lines will result in an exception being thrown.
 * </p>
 */
public final class PatchAnalyzer {
    /** The patch to analyze. */
    private final Patch patch;

    /**
     * Initializes a new instance of the {@code PatchAnalyzer} class.
     *
     * @param patch
     *        The patch to analyze.
     *
     * @throws IllegalArgumentException
     *         If {@code patch} contains any context lines.
     */
    public PatchAnalyzer(final Patch patch) {
        if (DiffLibUtils.Patch.isContextLinePresent(patch)) {
            throw new IllegalArgumentException("patch must not contain any context lines"); //$NON-NLS-1$
        }

        this.patch = patch;
    }

    /**
     * Gets the delta associated with the specified line.
     *
     * @param lineIndex
     *        The zero-based index of the line whose delta is desired; must not
     *        be negative.
     *
     * @return The delta associated with the specified line or {@code null} if
     *         none.
     */
    private @Nullable Delta getDeltaForLine(final int lineIndex) {
        assert lineIndex >= 0;

        for (final Delta delta : patch.getDeltas()) {
            final Chunk chunk = delta.getRevised();
            if (DiffLibUtils.RemoveChunk.isLineIncluded(chunk, lineIndex)
                    || DiffLibUtils.AddOrChangeChunk.isLineIncluded(chunk, lineIndex)) {
                return delta;
            }
        }

        return null;
    }

    /**
     * Gets the type of dirty mark associated with the specified line.
     *
     * @param lineIndex
     *        The zero-based index of the line whose dirty mark is desired; must
     *        not be negative.
     *
     * @return The type of dirty mark associated with the specified line.
     */
    public DirtyMarkType getDirtyMarkForLine(final int lineIndex) {
        assert lineIndex >= 0;

        boolean isContentRemovedAboveThisLine = false;
        boolean isContentRemovedBelowThisLine = false;

        final Delta deltaForThisLine = getDeltaForLine(lineIndex);
        if (deltaForThisLine != null) {
            if (DiffLibUtils.Delta.isContentAdded(deltaForThisLine)) {
                return DirtyMarkType.ADDED;
            } else if (DiffLibUtils.Delta.isContentChanged(deltaForThisLine)) {
                return DirtyMarkType.CHANGED;
            } else {
                isContentRemovedAboveThisLine = DiffLibUtils.Delta.isContentRemoved(deltaForThisLine);
            }
        }

        final Delta deltaForNextLine = getDeltaForLine(lineIndex + 1);
        if (deltaForNextLine != null) {
            final boolean isContentRemovedAboveNextLine = DiffLibUtils.Delta.isContentRemoved(deltaForNextLine);
            isContentRemovedBelowThisLine = isContentRemovedAboveNextLine;
        }

        if (isContentRemovedAboveThisLine && !isContentRemovedBelowThisLine) {
            return DirtyMarkType.REMOVED_ABOVE;
        } else if (!isContentRemovedAboveThisLine && isContentRemovedBelowThisLine) {
            return DirtyMarkType.REMOVED_BELOW;
        } else if (isContentRemovedAboveThisLine && isContentRemovedBelowThisLine) {
            return DirtyMarkType.REMOVED_ABOVE_AND_BELOW;
        }

        return DirtyMarkType.UNCHANGED;
    }
}