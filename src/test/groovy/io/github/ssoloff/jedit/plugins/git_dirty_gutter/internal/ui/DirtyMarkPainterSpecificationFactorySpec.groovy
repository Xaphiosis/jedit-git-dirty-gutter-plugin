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
package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.ui

import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.ADDED
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.CHANGED
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.REMOVED_ABOVE
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.REMOVED_ABOVE_AND_BELOW
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.REMOVED_BELOW
import static io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.DirtyMarkType.UNCHANGED

import java.awt.Color
import spock.lang.Specification

class DirtyMarkPainterSpecificationFactorySpec extends Specification {
    private static final ADDED_DIRTY_MARK_COLOR = Color.CYAN
    private static final CHANGED_DIRTY_MARK_COLOR = Color.BLUE
    private static final REMOVED_DIRTY_MARK_COLOR = Color.MAGENTA

    private final context = Stub(IDirtyMarkPainterSpecificationFactoryContext) {
        getAddedDirtyMarkColor() >> ADDED_DIRTY_MARK_COLOR
        getChangedDirtyMarkColor() >> CHANGED_DIRTY_MARK_COLOR
        getRemovedDirtyMarkColor() >> REMOVED_DIRTY_MARK_COLOR
    }
    private final factory = new DirtyMarkPainterSpecificationFactory(context)

    def 'createDirtyMarkPainterSpecification - when dirty mark type is ADDED it should return an added dirty mark painter specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(ADDED)

        then:
        specification.bodyPainted == true
        specification.bottomStripPainted == false
        specification.color == ADDED_DIRTY_MARK_COLOR
        specification.topStripPainted == false
    }

    def 'createDirtyMarkPainterSpecification - when dirty mark type is CHANGED it should return a changed dirty mark painter specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(CHANGED)

        then:
        specification.bodyPainted == true
        specification.bottomStripPainted == false
        specification.color == CHANGED_DIRTY_MARK_COLOR
        specification.topStripPainted == false
    }

    def 'createDirtyMarkPainterSpecification - when dirty mark type is REMOVED_ABOVE it should return a removed above dirty mark painter specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(REMOVED_ABOVE)

        then:
        specification.bodyPainted == false
        specification.bottomStripPainted == false
        specification.color == REMOVED_DIRTY_MARK_COLOR
        specification.topStripPainted == true
    }

    def 'createDirtyMarkPainterSpecification - when dirty mark type is REMOVED_BELOW it should return a removed below dirty mark painter specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(REMOVED_BELOW)

        then:
        specification.bodyPainted == false
        specification.bottomStripPainted == true
        specification.color == REMOVED_DIRTY_MARK_COLOR
        specification.topStripPainted == false
    }

    def 'createDirtyMarkPainterSpecification - when dirty mark type is REMOVED_ABOVE_AND_BELOW it should return a removed above and below dirty mark painter specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(REMOVED_ABOVE_AND_BELOW)

        then:
        specification.bodyPainted == false
        specification.bottomStripPainted == true
        specification.color == REMOVED_DIRTY_MARK_COLOR
        specification.topStripPainted == true
    }

    def 'createDirtyMarkPainterSpecification - when dirty mark type is UNCHANGED it should return null object'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(UNCHANGED)

        then:
        specification == DirtyMarkPainterSpecification.NULL
    }
}
