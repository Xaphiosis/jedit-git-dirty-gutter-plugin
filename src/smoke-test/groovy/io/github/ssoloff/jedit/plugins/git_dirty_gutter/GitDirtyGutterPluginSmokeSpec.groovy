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

import java.nio.file.Paths
import javax.swing.JComboBox
import org.apache.commons.io.FileUtils
import org.fest.swing.core.GenericTypeMatcher
import org.fest.swing.timing.Pause
import org.gjt.sp.jedit.jEdit
import org.gjt.sp.jedit.testframework.TestUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Title

@Title('Smoke tests for Git DirtyBuffer plugin')
class GitDirtyBufferPluginSmokeSpec extends Specification {
    private static final String GIT_DIRTY_LINE_PROVIDER_ITEM = 'Git'
    private static final String PROP_DIRTY_LINE_PROVIDER = 'options.LCMPlugin.provider'
    private static final String SYS_PROP_FUNCTIONAL_TEST_PLUGIN_JARS_DIR =
        'io.github.ssoloff.functionalTestPluginJarsDir'

    @Rule
    @SuppressWarnings('PublicInstanceField')
    public final TemporaryFolder temporaryFolder = new TemporaryFolder()

    private static void installPlugins(settingsDir) {
        def settingsDirPath = settingsDir.toPath()
        def settingsPluginJarsDirPath = settingsDirPath.resolve('jars')
        settingsPluginJarsDirPath.toFile().mkdirs()

        def functionalTestPluginJarsDirPath = Paths.get(System.getProperty(SYS_PROP_FUNCTIONAL_TEST_PLUGIN_JARS_DIR))
        FileUtils.copyDirectory(functionalTestPluginJarsDirPath.toFile(), settingsPluginJarsDirPath.toFile())
    }

    private static newDirtyLineProviderComboBoxMatcher() {
        // dirty line provider combo box has no name but is the only combo box in the panel
        new GenericTypeMatcher<JComboBox>(JComboBox, true) {
            @Override
            boolean isMatching(JComboBox component) {
                true
            }
        }
    }

    private void propertyMatches(name, value) {
        waitForCondition(1000) {
            assert jEdit.getProperty(name) == value
        }
    }

    private void waitForCondition(long timeoutInMilliseconds, Closure condition) {
        def startTimeInMilliseconds = System.currentTimeMillis()
        while (true) {
            try {
                condition()
                return
            } catch (AssertionError e) {
                if (System.currentTimeMillis() - startTimeInMilliseconds > timeoutInMilliseconds) {
                    throw e
                } else {
                    Pause.pause(10)
                }
            }
        }
    }

    def setup() {
        def settingsDir = temporaryFolder.newFolder()
        System.setProperty TestUtils.ENV_JEDIT_SETTINGS, settingsDir.path
        installPlugins(settingsDir)
        TestUtils.setupNewjEdit()
    }

    def cleanup() {
        TestUtils.tearDownNewjEdit()
        TestUtils.tearDownNewjEdit() // HACK: work around logic bug: call twice to hit all code paths
    }

    def 'it should be enableable from the plugin options dialog'() {
        given: 'the plugin options dialog is open'
        def optionsDialog = TestUtils.pluginOptions()

        and: 'the DirtyGutter plugin options are displayed'
        def optionPane = optionsDialog.optionPane('DirtyGutter', 'DirtyGutter')

        when: 'the Git dirty line provider is selected'
        def comboBox = optionPane.comboBox(newDirtyLineProviderComboBoxMatcher())
        comboBox.selectItem(GIT_DIRTY_LINE_PROVIDER_ITEM)

        and: 'the plugin options dialog is closed with OK'
        optionsDialog.OK()

        then: 'the Git dirty line provider should be enabled'
        propertyMatches(PROP_DIRTY_LINE_PROVIDER, GIT_DIRTY_LINE_PROVIDER_ITEM)
    }
}
