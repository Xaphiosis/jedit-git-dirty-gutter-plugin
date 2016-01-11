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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util;

/**
 * Provides methods for creating various types of suppliers.
 */
public final class Suppliers {
    private Suppliers() {
    }

    /**
     * Creates a new supplier for the specified object.
     *
     * @param <T>
     *        The type of the object to be supplied.
     *
     * @param obj
     *        The object to be supplied.
     *
     * @return A new supplier for the specified object.
     */
    public static <T> ISupplier<T> forObject(final T obj) {
        return new ISupplier<T>() {
            @Override
            public T get() {
                return obj;
            }
        };
    }
}
