/*
 * Copyright (C) 2016 Steven Soloff
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple auto-reset event for synchronizing between two threads.
 */
public final class AutoResetEvent {
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private boolean signaled = false;

    /**
     * Causes the current thread to wait until the event is signaled,
     * interrupted, or the specified waiting time elapses.
     *
     * @param time
     *        The maximum time to wait.
     * @param unit
     *        The time unit of the {@code time} argument.
     *
     * @return {@code true} if the event was signaled before the specified
     *         waiting time has elapsed; otherwise {@code false}.
     *
     * @throws InterruptedException
     *         If the current thread is interrupted.
     */
    public boolean await(final long time, final TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            if (!signaled && !condition.await(time, unit)) {
                return false;
            }
            signaled = false;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Wakes up one thread waiting for the event.
     */
    public void signal() {
        lock.lock();
        try {
            signaled = true;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
}
