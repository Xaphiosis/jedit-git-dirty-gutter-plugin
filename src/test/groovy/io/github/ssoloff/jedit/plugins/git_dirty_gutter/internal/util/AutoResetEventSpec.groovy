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

import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import spock.lang.Specification

class AutoResetEventSpec extends Specification {
    private def event = new AutoResetEvent()

    def 'await - when signaled within the waiting period it should return true'() {
        setup:
        def barrier = new CyclicBarrier(2)
        def executorService = Executors.newCachedThreadPool()
        def task = executorService.submit({
            barrier.await()
            event.await(5, TimeUnit.SECONDS)
        } as Callable<Boolean>)

        when:
        barrier.await()
        event.signal()

        then:
        def result = task.get(5, TimeUnit.SECONDS)
        result == true

        cleanup:
        executorService.shutdown()
    }

    def 'await - when not signaled within the waiting period it should return false'() {
        when:
        def result = event.await(1, TimeUnit.MILLISECONDS)

        then:
        result == false
    }
}