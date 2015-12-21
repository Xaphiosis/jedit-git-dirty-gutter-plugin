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
        // --- from.add	2015-12-21 08:41:37.902610985 -0500
        // +++ to.add	2015-12-21 08:41:52.663714666 -0500
        // @@ -9,0 +10 @@
        // +10

        setup:
        def patch = new Patch()
        patch.addDelta(new ChangeDelta(
            new Chunk(8, []),
            new Chunk(9, ['10'])
        ))
        def classifier = new DirtyMarkClassifier(patch)

        expect:
        classifier.classifyLine(8) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(9) == DirtyMarkType.ADDED
        classifier.classifyLine(10) == DirtyMarkType.UNCHANGED
    }

    def 'classifyLine should classify a changed line'() {
        // --- from.change	2015-12-21 09:09:52.392320598 -0500
        // +++ to.change	2015-12-21 09:10:01.216381102 -0500
        // @@ -10 +10 @@
        // -10/from
        // +10/to

        setup:
        def patch = new Patch()
        patch.addDelta(new ChangeDelta(
            new Chunk(9, ['10/from']),
            new Chunk(9, ['10/to'])
        ))
        def classifier = new DirtyMarkClassifier(patch)

        expect:
        classifier.classifyLine(8) == DirtyMarkType.UNCHANGED
        classifier.classifyLine(9) == DirtyMarkType.CHANGED
        classifier.classifyLine(10) == DirtyMarkType.UNCHANGED
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
            new Chunk(0, [])
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
}
