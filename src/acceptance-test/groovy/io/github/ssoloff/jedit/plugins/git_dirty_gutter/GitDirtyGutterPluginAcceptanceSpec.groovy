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
package io.github.ssoloff.jedit.plugins.git_dirty_gutter

import org.gjt.sp.jedit.testframework.TestUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Title

@Title('Acceptance tests for Git DirtyBuffer plugin')
class GitDirtyBufferPluginAcceptanceSpec extends Specification {
    @Rule
    @SuppressWarnings('PublicInstanceField')
    public final TemporaryFolder temporaryFolder = new TemporaryFolder()

    def setup() {
        def settingsFolder = temporaryFolder.newFolder()
        System.setProperty TestUtils.ENV_JEDIT_SETTINGS, settingsFolder.path
        TestUtils.setupNewjEdit()
    }

    def cleanup() {
        TestUtils.tearDownNewjEdit()
        TestUtils.tearDownNewjEdit() // HACK: work around logic bug: call twice to hit all code paths
    }

    def 'TODO'() {
        when: 'TODO'
        Thread.sleep(1000)

        then: 'TODO'
        true
    }
}
