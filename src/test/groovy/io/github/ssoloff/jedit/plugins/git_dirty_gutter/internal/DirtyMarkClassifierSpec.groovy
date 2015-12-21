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

class DirtyMarkClassifierSpec extends Specification {
    def 'ctor should throw exception if patch contains context lines'() {
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
        new DirtyMarkClassifier(patch)

        then:
        thrown(IllegalArgumentException)
    }

    def 'classifyLine should classify an added line'() {
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
        def classifier = new DirtyMarkClassifier(patch)

        expect:
        classifier.classifyLine(0) == DirtyMarkType.ADDED
        classifier.classifyLine(1) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(8) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(9) == DirtyMarkType.ADDED
        classifier.classifyLine(10) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(18) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(19) == DirtyMarkType.ADDED
    }

    def 'classifyLine should classify a changed line'() {
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
        def classifier = new DirtyMarkClassifier(patch)

        expect:
        classifier.classifyLine(0) == DirtyMarkType.CHANGED
        classifier.classifyLine(1) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(8) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(9) == DirtyMarkType.CHANGED
        classifier.classifyLine(10) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(18) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(19) == DirtyMarkType.CHANGED
    }

    def 'classifyLine should classify a removed line'() {
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
        def classifier = new DirtyMarkClassifier(patch)

        expect:
        classifier.classifyLine(0) == DirtyMarkType.REMOVED_ABOVE
        classifier.classifyLine(1) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(6) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(7) == DirtyMarkType.REMOVED_BELOW
        classifier.classifyLine(8) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(15) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(16) == DirtyMarkType.REMOVED_BELOW
    }

    def 'classifyLine should classify a mixed collection of added, changed, and removed lines'() {
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
        def classifier = new DirtyMarkClassifier(patch)

        expect:
        classifier.classifyLine(0) == DirtyMarkType.ADDED
        classifier.classifyLine(1) == DirtyMarkType.ADDED
        classifier.classifyLine(2) == DirtyMarkType.ADDED
        classifier.classifyLine(3) == DirtyMarkType.ADDED
        classifier.classifyLine(4) == DirtyMarkType.ADDED
        classifier.classifyLine(5) == DirtyMarkType.ADDED
        classifier.classifyLine(6) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(7) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(8) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(9) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(10) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(11) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(12) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(13) == DirtyMarkType.CHANGED
        classifier.classifyLine(14) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(15) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(16) == DirtyMarkType.CHANGED
        classifier.classifyLine(17) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(18) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(19) == DirtyMarkType.REMOVED_BELOW
        classifier.classifyLine(20) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(21) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(22) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(23) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(24) == DirtyMarkType.ADDED
        classifier.classifyLine(25) == DirtyMarkType.ADDED
        classifier.classifyLine(26) == DirtyMarkType.ADDED
        classifier.classifyLine(27) == DirtyMarkType.ADDED
    }
}
