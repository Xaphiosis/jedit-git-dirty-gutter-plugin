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
package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Subject(Suppliers)
@Title('Unit tests for Suppliers#forObject')
class Suppliers_ForObjectSpec extends Specification {
    def 'it should return a supplier that provides the specified object'() {
        given: 'an object'
        def object = new Object()

        when: 'creating a supplier for the object'
        def supplier = Suppliers.forObject(object)

        then: 'the supplier should provide the object'
        supplier.get() == object
    }
}
