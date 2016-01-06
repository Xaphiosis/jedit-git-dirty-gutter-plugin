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
package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model

import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.*

import difflib.ChangeDelta
import difflib.Chunk
import difflib.DiffUtils
import difflib.Patch
import spock.lang.Specification

class PatchAnalyzerSpec extends Specification {
    private newPatch(oldLines, newLines) {
        DiffUtils.diff(oldLines, newLines)
    }

    def 'ctor - when patch contains context lines it should throw an exception'() {
        setup:
        def patch = new Patch()
        patch.addDelta(new ChangeDelta(
            new Chunk(8, ['9', '10/old', '11']),
            new Chunk(8, ['9', '10/new', '11'])
        ))

        when:
        new PatchAnalyzer(patch)

        then:
        thrown(IllegalArgumentException)
    }

    def 'getDirtyMarkForLine - when line index is negative it should throw an exception'() {
        setup:
        def patchAnalyzer = new PatchAnalyzer(newPatch([], []))

        when:
        patchAnalyzer.getDirtyMarkForLine(-1)

        then:
        thrown(IllegalArgumentException)
    }

    def 'getDirtyMarkForLine - it should handle an empty patch'() {
        setup:
        def patchAnalyzer = new PatchAnalyzer(new Patch())

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [0, 1, 2]
        dirtyMarkType << [UNCHANGED, UNCHANGED, UNCHANGED]
    }

    def 'getDirtyMarkForLine - it should handle addition of the first line'() {
        setup:
        def oldLines = [     '2', '3', '']
        def newLines = ['1', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [0, 1]
        dirtyMarkType << [ADDED, UNCHANGED]
    }

    def 'getDirtyMarkForLine - it should handle addition of an intermediate line'() {
        setup:
        def oldLines = ['1',      '3', '']
        def newLines = ['1', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [0, 1, 2]
        dirtyMarkType << [UNCHANGED, ADDED, UNCHANGED]
    }

    def 'getDirtyMarkForLine - it should handle addition of the last line'() {
        setup:
        def oldLines = ['1', '2',      '']
        def newLines = ['1', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [1, 2, 3]
        dirtyMarkType << [UNCHANGED, ADDED, UNCHANGED]
    }

    def 'getDirtyMarkForLine - it should handle addition of the final newline'() {
        setup:
        def oldLines = ['1', '2', '3'    ]
        def newLines = ['1', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [2, 3]
        dirtyMarkType << [UNCHANGED, ADDED]
    }

    def 'getDirtyMarkForLine - it should handle modification of the first line'() {
        setup:
        def oldLines = ['1/old', '2', '3', '']
        def newLines = ['1/new', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [0, 1]
        dirtyMarkType << [CHANGED, UNCHANGED]
    }

    def 'getDirtyMarkForLine - it should handle modification of an intermediate line'() {
        setup:
        def oldLines = ['1', '2/old', '3', '']
        def newLines = ['1', '2/new', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [0, 1, 2]
        dirtyMarkType << [UNCHANGED, CHANGED, UNCHANGED]
    }

    def 'getDirtyMarkForLine - it should handle modification of the last line'() {
        setup:
        def oldLines = ['1', '2', '3/old', '']
        def newLines = ['1', '2', '3/new', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [1, 2, 3]
        dirtyMarkType << [UNCHANGED, CHANGED, UNCHANGED]
    }

    def 'getDirtyMarkForLine - it should handle removal of the first line'() {
        setup:
        def oldLines = ['1', '2', '3', '']
        def newLines = [     '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [0, 1]
        dirtyMarkType << [REMOVED_ABOVE, UNCHANGED]
    }

    def 'getDirtyMarkForLine - it should handle removal of an intermediate line'() {
        setup:
        def oldLines = ['1', '2', '3', '4', '5', '']
        def newLines = ['1', '2',      '4', '5', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [0, 1, 2, 3]
        dirtyMarkType << [UNCHANGED, REMOVED_BELOW, REMOVED_ABOVE, UNCHANGED]
    }

    def 'getDirtyMarkForLine - it should handle removal of the last line'() {
        setup:
        def oldLines = ['1', '2', '3', '']
        def newLines = ['1', '2',      '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [0, 1, 2]
        dirtyMarkType << [UNCHANGED, REMOVED_BELOW, REMOVED_ABOVE]
    }

    def 'getDirtyMarkForLine - it should handle removal of lines above and below a single line'() {
        setup:
        def oldLines = ['1', '2', '3', '4', '5', '']
        def newLines = ['1',      '3',      '5', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [0, 1, 2]
        dirtyMarkType << [REMOVED_BELOW, REMOVED_ABOVE_AND_BELOW, REMOVED_ABOVE]
    }

    def 'getDirtyMarkForLine - it should handle removal of the final newline'() {
        setup:
        def oldLines = ['1', '2', '3', '']
        def newLines = ['1', '2', '3'    ]
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << [1, 2]
        dirtyMarkType << [UNCHANGED, REMOVED_BELOW]
    }

    def 'getDirtyMarkForLine - it should handle a mixed collection of added, changed, and removed lines'() {
        setup:
        def oldLines = [
            'This part of the',
            'document has stayed the',
            'same from version to',
            'version.  It shouldn\'t',
            'be shown if it doesn\'t',
            'change.  Otherwise, that',
            'would not be helping to',
            'compress the size of the',
            'changes.',
            '',
            'This paragraph contains',
            'text that is outdated.',
            'It will be deleted in the',
            'near future.',
            '',
            'It is important to spell',
            'check this dokument. On',
            'the other hand, a',
            'misspelled word isn\'t',
            'the end of the world.',
            'Nothing in the rest of',
            'this paragraph needs to',
            'be changed. Things can',
            'be added after it.',
            ''
        ]
        def newLines = [
            'This is an important',
            'notice! It should',
            'therefore be located at',
            'the beginning of this',
            'document!',
            '',
            'This part of the',
            'document has stayed the',
            'same from version to',
            'version.  It shouldn\'t',
            'be shown if it doesn\'t',
            'change.  Otherwise, that',
            'would not be helping to',
            'compress anything.',
            '',
            'It is important to spell',
            'check this document. On',
            'the other hand, a',
            'misspelled word isn\'t',
            'the end of the world.',
            'Nothing in the rest of',
            'this paragraph needs to',
            'be changed. Things can',
            'be added after it.',
            '',
            'This paragraph contains',
            'important new additions',
            'to this document.',
            ''
        ]
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        expect:
        patchAnalyzer.getDirtyMarkForLine(lineIndex) == dirtyMarkType

        where:
        lineIndex << (0..28).toList()
        dirtyMarkType << [
            ADDED, ADDED, ADDED, ADDED, ADDED,
            ADDED, UNCHANGED, UNCHANGED, UNCHANGED, UNCHANGED,
            UNCHANGED, UNCHANGED, UNCHANGED, CHANGED, REMOVED_BELOW,
            REMOVED_ABOVE, CHANGED, UNCHANGED, UNCHANGED, UNCHANGED,
            UNCHANGED, UNCHANGED, UNCHANGED, UNCHANGED, UNCHANGED,
            ADDED, ADDED, ADDED, ADDED
        ]
    }
}
