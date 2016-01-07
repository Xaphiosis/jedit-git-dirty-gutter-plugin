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

class DirtyMarkPainterSpecificationFactory_CreateDirtyMarkPainterSpecificationSpec extends Specification {
    private static final ADDED_DIRTY_MARK_COLOR = Color.CYAN
    private static final CHANGED_DIRTY_MARK_COLOR = Color.BLUE
    private static final REMOVED_DIRTY_MARK_COLOR = Color.MAGENTA

    private final factory = newDirtyMarkPainterSpecificationFactory()

    @SuppressWarnings('UnnecessaryGetter')
    private newDirtyMarkPainterSpecificationFactory() {
        def context = Stub(IDirtyMarkPainterSpecificationFactoryContext) {
            getAddedDirtyMarkColor() >> ADDED_DIRTY_MARK_COLOR
            getChangedDirtyMarkColor() >> CHANGED_DIRTY_MARK_COLOR
            getRemovedDirtyMarkColor() >> REMOVED_DIRTY_MARK_COLOR
        }
        new DirtyMarkPainterSpecificationFactory(context)
    }

    def 'when dirty mark type is ADDED it should return an added specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(ADDED)

        then:
        with(specification) {
            bodyPainted == true
            bottomStripPainted == false
            color == ADDED_DIRTY_MARK_COLOR
            topStripPainted == false
        }
    }

    def 'when dirty mark type is CHANGED it should return a changed specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(CHANGED)

        then:
        with(specification) {
            bodyPainted == true
            bottomStripPainted == false
            color == CHANGED_DIRTY_MARK_COLOR
            topStripPainted == false
        }
    }

    def 'when dirty mark type is REMOVED_ABOVE it should return a removed above specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(REMOVED_ABOVE)

        then:
        with(specification) {
            bodyPainted == false
            bottomStripPainted == false
            color == REMOVED_DIRTY_MARK_COLOR
            topStripPainted == true
        }
    }

    def 'when dirty mark type is REMOVED_BELOW it should return a removed below specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(REMOVED_BELOW)

        then:
        with(specification) {
            bodyPainted == false
            bottomStripPainted == true
            color == REMOVED_DIRTY_MARK_COLOR
            topStripPainted == false
        }
    }

    def 'when dirty mark type is REMOVED_ABOVE_AND_BELOW it should return a removed above and below specification'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(REMOVED_ABOVE_AND_BELOW)

        then:
        with(specification) {
            bodyPainted == false
            bottomStripPainted == true
            color == REMOVED_DIRTY_MARK_COLOR
            topStripPainted == true
        }
    }

    def 'when dirty mark type is UNCHANGED it should return null object'() {
        when:
        def specification = factory.createDirtyMarkPainterSpecification(UNCHANGED)

        then:
        specification == DirtyMarkPainterSpecification.NULL
    }
}
