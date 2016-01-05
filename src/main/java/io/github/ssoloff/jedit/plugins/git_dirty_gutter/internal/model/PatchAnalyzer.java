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

    private @Nullable DirtyMarkType getAddedOrChangedDirtyMarkForLine(final int lineIndex) {
        assert lineIndex >= 0;

        final Delta deltaForThisLine = getDeltaForLine(lineIndex);
        if (deltaForThisLine != null) {
            if (DiffLibUtils.Delta.isContentAdded(deltaForThisLine)) {
                return DirtyMarkType.ADDED;
            } else if (DiffLibUtils.Delta.isContentChanged(deltaForThisLine)) {
                return DirtyMarkType.CHANGED;
            }
        }

        return null;
    }

    /**
     * Gets the type of dirty mark associated with the specified line.
     *
     * @param lineIndex
     *        The zero-based index of the line whose dirty mark is desired.
     *
     * @return The type of dirty mark associated with the specified line.
     *
     * @throws IllegalArgumentException
     *         If {@code lineIndex} is negative.
     */
    public DirtyMarkType getDirtyMarkForLine(final int lineIndex) {
        if (lineIndex < 0) {
            throw new IllegalArgumentException("line index must not be negative"); //$NON-NLS-1$
        }

        final DirtyMarkType addedOrChangedDirtyMarkType = getAddedOrChangedDirtyMarkForLine(lineIndex);
        if (addedOrChangedDirtyMarkType != null) {
            return addedOrChangedDirtyMarkType;
        }

        final DirtyMarkType removedDirtyMarkType = getRemovedDirtyMarkForLine(lineIndex);
        if (removedDirtyMarkType != null) {
            return removedDirtyMarkType;
        }

        return DirtyMarkType.UNCHANGED;
    }

    private @Nullable DirtyMarkType getRemovedDirtyMarkForLine(final int lineIndex) {
        assert lineIndex >= 0;

        boolean contentRemovedAboveThisLine = false;
        final Delta deltaForThisLine = getDeltaForLine(lineIndex);
        if (deltaForThisLine != null) {
            contentRemovedAboveThisLine = DiffLibUtils.Delta.isContentRemoved(deltaForThisLine);
        }

        boolean contentRemovedBelowThisLine = false;
        final Delta deltaForNextLine = getDeltaForLine(lineIndex + 1);
        if (deltaForNextLine != null) {
            final boolean contentRemovedAboveNextLine = DiffLibUtils.Delta.isContentRemoved(deltaForNextLine);
            contentRemovedBelowThisLine = contentRemovedAboveNextLine;
        }

        if (contentRemovedAboveThisLine && !contentRemovedBelowThisLine) {
            return DirtyMarkType.REMOVED_ABOVE;
        } else if (!contentRemovedAboveThisLine && contentRemovedBelowThisLine) {
            return DirtyMarkType.REMOVED_BELOW;
        } else if (contentRemovedAboveThisLine && contentRemovedBelowThisLine) {
            return DirtyMarkType.REMOVED_ABOVE_AND_BELOW;
        }

        return null;
    }
}
