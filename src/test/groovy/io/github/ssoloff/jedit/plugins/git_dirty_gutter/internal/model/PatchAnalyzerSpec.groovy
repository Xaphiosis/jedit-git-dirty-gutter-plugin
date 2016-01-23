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
package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model

import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.ADDED
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.CHANGED
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.REMOVED_ABOVE
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.REMOVED_ABOVE_AND_BELOW
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.REMOVED_BELOW
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.UNCHANGED

import difflib.ChangeDelta
import difflib.Chunk
import difflib.DiffUtils
import difflib.Patch
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Subject(PatchAnalyzer)
@Title('Unit tests for PatchAnalyzer#PatchAnalyzer')
class PatchAnalyzer_CtorSpec extends Specification {
    @SuppressWarnings('UnusedObject')
    def 'when patch contains context lines it should throw an exception'() {
        given: 'a patch that contains context lines'
        def patch = new Patch()
        patch.addDelta(new ChangeDelta(
            new Chunk(8, ['9', '10/old', '11']),
            new Chunk(8, ['9', '10/new', '11'])
        ))

        when: 'instantiating a PatchAnalyzer'
        new PatchAnalyzer(patch)

        then: 'it should throw an exception'
        thrown(IllegalArgumentException)
    }
}

@Subject(PatchAnalyzer)
@Title('Unit tests for PatchAnalyzer#getDirtyMarkForLine')
class PatchAnalyzer_GetDirtyMarkForLineSpec extends Specification {
    private static newPatch(oldLines, newLines) {
        DiffUtils.diff(oldLines, newLines)
    }

    def 'when line index is negative it should throw an exception'() {
        given: 'an empty patch'
        def patchAnalyzer = new PatchAnalyzer(newPatch([], []))

        when: 'getting the dirty mark for line -1'
        patchAnalyzer.getDirtyMarkForLine(-1)

        then: 'it should throw an exception'
        thrown(IllegalArgumentException)
    }

    def 'it should handle an empty patch'() {
        given: 'an empty patch'
        def patchAnalyzer = new PatchAnalyzer(new Patch())

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || UNCHANGED
        1         || UNCHANGED
        2         || UNCHANGED
    }

    def 'it should handle addition of the first line'() {
        given: 'a patch describing an addition at line 0 in range [0,2]'
        def oldLines = [     '2', '3', '']
        def newLines = ['1', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || ADDED
        1         || UNCHANGED
        2         || UNCHANGED
        3         || UNCHANGED
    }

    def 'it should handle addition of an intermediate line'() {
        given: 'a patch describing an addition at line 1 in range [0,2]'
        def oldLines = ['1',      '3', '']
        def newLines = ['1', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || UNCHANGED
        1         || ADDED
        2         || UNCHANGED
        3         || UNCHANGED
    }

    def 'it should handle addition of the last line'() {
        given: 'a patch describing an addition at line 2 in range [0,2]'
        def oldLines = ['1', '2',      '']
        def newLines = ['1', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || UNCHANGED
        1         || UNCHANGED
        2         || ADDED
        3         || UNCHANGED
    }

    def 'it should handle addition of the final newline'() {
        given: 'a patch describing an addition at line 3 in range [0,2]'
        def oldLines = ['1', '2', '3'    ]
        def newLines = ['1', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || UNCHANGED
        1         || UNCHANGED
        2         || UNCHANGED
        3         || ADDED
    }

    def 'it should handle modification of the first line'() {
        given: 'a patch describing a modification at line 0 in range [0,3]'
        def oldLines = ['1/old', '2', '3', '']
        def newLines = ['1/new', '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || CHANGED
        1         || UNCHANGED
        2         || UNCHANGED
        3         || UNCHANGED
    }

    def 'it should handle modification of an intermediate line'() {
        given: 'a patch describing a modification at line 1 in range [0,3]'
        def oldLines = ['1', '2/old', '3', '']
        def newLines = ['1', '2/new', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || UNCHANGED
        1         || CHANGED
        2         || UNCHANGED
        3         || UNCHANGED
    }

    def 'it should handle modification of the last line'() {
        given: 'a patch describing a modification at line 2 in range [0,3]'
        def oldLines = ['1', '2', '3/old', '']
        def newLines = ['1', '2', '3/new', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || UNCHANGED
        1         || UNCHANGED
        2         || CHANGED
        3         || UNCHANGED
    }

    def 'it should handle removal of the first line'() {
        given: 'a patch describing a removal at line 0 in range [0,3]'
        def oldLines = ['1', '2', '3', '']
        def newLines = [     '2', '3', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || REMOVED_ABOVE
        1         || UNCHANGED
        2         || UNCHANGED
    }

    def 'it should handle removal of an intermediate line'() {
        given: 'a patch describing a removal at line 2 in range [0,5]'
        def oldLines = ['1', '2', '3', '4', '5', '']
        def newLines = ['1', '2',      '4', '5', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || UNCHANGED
        1         || REMOVED_BELOW
        2         || REMOVED_ABOVE
        3         || UNCHANGED
        4         || UNCHANGED
    }

    def 'it should handle removal of the last line'() {
        given: 'a patch describing a removal at line 2 in range [0,3]'
        def oldLines = ['1', '2', '3', '']
        def newLines = ['1', '2',      '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || UNCHANGED
        1         || REMOVED_BELOW
        2         || REMOVED_ABOVE
    }

    def 'it should handle removal of lines above and below a single line'() {
        given: 'a patch describing a removal at lines 1 and 3 in range [0,5]'
        def oldLines = ['1', '2', '3', '4', '5', '']
        def newLines = ['1',      '3',      '5', '']
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || REMOVED_BELOW
        1         || REMOVED_ABOVE_AND_BELOW
        2         || REMOVED_ABOVE
        3         || UNCHANGED
    }

    def 'it should handle removal of the final newline'() {
        given: 'a patch describing a removal at line 3 in range [0,3]'
        def oldLines = ['1', '2', '3', '']
        def newLines = ['1', '2', '3'    ]
        def patchAnalyzer = new PatchAnalyzer(newPatch(oldLines, newLines))

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || UNCHANGED
        1         || UNCHANGED
        2         || REMOVED_BELOW
    }

    @SuppressWarnings(['CyclomaticComplexity', 'MethodSize'])
    def 'it should handle a mixed collection of added, changed, and removed lines'() {
        given: 'a patch describing a mixed collection of added, changed, and removed lines'
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

        when: 'getting the dirty mark for line #lineIndex'
        def result = patchAnalyzer.getDirtyMarkForLine(lineIndex)

        then: 'it should be #dirtyMarkType'
        result == dirtyMarkType

        where:
        lineIndex || dirtyMarkType
        0         || ADDED
        1         || ADDED
        2         || ADDED
        3         || ADDED
        4         || ADDED
        5         || ADDED
        6         || UNCHANGED
        7         || UNCHANGED
        8         || UNCHANGED
        9         || UNCHANGED
        10        || UNCHANGED
        11        || UNCHANGED
        12        || UNCHANGED
        13        || CHANGED
        14        || REMOVED_BELOW
        15        || REMOVED_ABOVE
        16        || CHANGED
        17        || UNCHANGED
        18        || UNCHANGED
        19        || UNCHANGED
        20        || UNCHANGED
        21        || UNCHANGED
        22        || UNCHANGED
        23        || UNCHANGED
        24        || UNCHANGED
        25        || ADDED
        26        || ADDED
        27        || ADDED
        28        || ADDED
    }
}
