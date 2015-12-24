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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal

import difflib.ChangeDelta
import difflib.Chunk
import difflib.Patch
import spock.lang.Specification

class PatchAnalyzerSpec extends Specification {
    def 'ctor - when patch contains context lines - should throw exception'() {
        // --- from.change	2015-12-21 09:09:52.392320598 -0500
        // +++ to.change	2015-12-21 09:10:01.216381102 -0500
        // @@ -9,3 +9,3 @@
        //  9
        // -10/from
        // +10/to
        //  11

        setup:
        def patch = new Patch()
        patch.addDelta(new ChangeDelta(
            new Chunk(8, ['9', '10/from', '11']),
            new Chunk(8, ['9', '10/to', '11'])
        ))

        when:
        new PatchAnalyzer(patch)

        then:
        thrown(IllegalArgumentException)
    }

    def 'getDirtyMarkForLine - should handle added lines'() {
        // --- from.add	2015-12-21 17:53:29.082877088 -0500
        // +++ to.add	2015-12-21 08:41:52.663714666 -0500
        // @@ -0,0 +1 @@
        // +1
        // @@ -8,0 +10 @@
        // +10
        // @@ -17,0 +20 @@
        // +20

        setup:
        def patch = new Patch()
        patch.addDelta(new ChangeDelta(
            new Chunk(0, []), // NB: difflib behavior: 0(1) -> -1(0) -> 0(0)
            new Chunk(0, ['1'])
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(7, []),
            new Chunk(9, ['10'])
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(16, []),
            new Chunk(19, ['20'])
        ))
        def patchAnalyzer = new PatchAnalyzer(patch)

        expect:
        patchAnalyzer.getDirtyMarkForLine(0) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(1) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(8) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(9) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(10) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(18) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(19) == DirtyMarkType.ADDED
    }

    def 'getDirtyMarkForLine - should handle changed lines'() {
        // --- from.change	2015-12-21 17:56:43.051161447 -0500
        // +++ to.change	2015-12-21 17:56:53.987233859 -0500
        // @@ -1 +1 @@
        // -1/from
        // +1/to
        // @@ -10 +10 @@
        // -10/from
        // +10/to
        // @@ -20 +20 @@
        // -20/from
        // +20/to

        setup:
        def patch = new Patch()
        patch.addDelta(new ChangeDelta(
            new Chunk(0, ['1/from']),
            new Chunk(0, ['1/to'])
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(9, ['10/from']),
            new Chunk(9, ['10/to'])
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(19, ['20/from']),
            new Chunk(19, ['20/to'])
        ))
        def patchAnalyzer = new PatchAnalyzer(patch)

        expect:
        patchAnalyzer.getDirtyMarkForLine(0) == DirtyMarkType.CHANGED
        patchAnalyzer.getDirtyMarkForLine(1) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(8) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(9) == DirtyMarkType.CHANGED
        patchAnalyzer.getDirtyMarkForLine(10) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(18) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(19) == DirtyMarkType.CHANGED
    }

    def 'getDirtyMarkForLine - should handle removed lines'() {
        // --- from.remove	2015-12-21 10:07:45.208277091 -0500
        // +++ to.remove	2015-12-21 10:07:54.855343987 -0500
        // @@ -1 +0,0 @@
        // -1
        // @@ -10 +8,0 @@
        // -10
        // @@ -20 +17,0 @@
        // -20

        setup:
        def patch = new Patch()
        patch.addDelta(new ChangeDelta(
            new Chunk(0, ['1']),
            new Chunk(0, []) // NB: difflib behavior: 0(1) -> -1(0) -> 0(0)
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(9, ['10']),
            new Chunk(7, [])
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(19, ['20']),
            new Chunk(16, [])
        ))
        def patchAnalyzer = new PatchAnalyzer(patch)

        expect:
        patchAnalyzer.getDirtyMarkForLine(0) == DirtyMarkType.REMOVED_ABOVE
        patchAnalyzer.getDirtyMarkForLine(1) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(6) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(7) == DirtyMarkType.REMOVED_BELOW
        patchAnalyzer.getDirtyMarkForLine(8) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(15) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(16) == DirtyMarkType.REMOVED_BELOW
    }

    def 'getDirtyMarkForLine - should handle a mixed collection of added, changed, and removed lines'() {
        // --- from.mixed	2015-12-21 17:33:49.971052965 -0500
        // +++ to.mixed	2015-12-21 17:34:43.429408252 -0500
        // @@ -0,0 +1,6 @@
        // +This is an important
        // +notice! It should
        // +therefore be located at
        // +the beginning of this
        // +document!
        // +
        // @@ -8,7 +14 @@
        // -compress the size of the
        // -changes.
        // -
        // -This paragraph contains
        // -text that is outdated.
        // -It will be deleted in the
        // -near future.
        // +compress anything.
        // @@ -17 +17 @@
        // -check this dokument. On
        // +check this document. On
        // @@ -21 +20,0 @@
        // -(pause)
        // @@ -25,0 +25,4 @@
        // +
        // +This paragraph contains
        // +important new additions
        // +to this document.

        setup:
        def patch = new Patch()
        patch.addDelta(new ChangeDelta(
            new Chunk(0, []), // NB: difflib behavior: 0(1) -> -1(0) -> 0(0)
            new Chunk(0, [
                'This is an important',
                'notice! It should',
                'therefore be located at',
                'the beginning of this',
                'document!',
                ''
            ])
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(7, [
                'compress the size of the',
                'changes.',
                '',
                'This paragraph contains',
                'text that is outdated.',
                'It will be deleted in the',
                'near future.'
            ]),
            new Chunk(13, [
                'compress anything.'
            ])
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(16, [
                'check this dokument. On'
            ]),
            new Chunk(16, [
                'check this document. On'
            ])
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(20, [
                '(pause)'
            ]),
            new Chunk(19, [])
        ))
        patch.addDelta(new ChangeDelta(
            new Chunk(24, []),
            new Chunk(24, [
                '',
                'This paragraph contains',
                'important new additions',
                'to this document.'
            ])
        ))
        def patchAnalyzer = new PatchAnalyzer(patch)

        expect:
        patchAnalyzer.getDirtyMarkForLine(0) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(1) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(2) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(3) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(4) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(5) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(6) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(7) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(8) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(9) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(10) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(11) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(12) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(13) == DirtyMarkType.CHANGED
        patchAnalyzer.getDirtyMarkForLine(14) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(15) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(16) == DirtyMarkType.CHANGED
        patchAnalyzer.getDirtyMarkForLine(17) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(18) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(19) == DirtyMarkType.REMOVED_BELOW
        patchAnalyzer.getDirtyMarkForLine(20) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(21) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(22) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(23) == DirtyMarkType.UNCHANGED
        patchAnalyzer.getDirtyMarkForLine(24) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(25) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(26) == DirtyMarkType.ADDED
        patchAnalyzer.getDirtyMarkForLine(27) == DirtyMarkType.ADDED
    }
}
