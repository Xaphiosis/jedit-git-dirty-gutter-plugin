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

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lcm.DirtyLineProviderOptions;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.ColorWellButton;

/**
 * Implementation of {@link DirtyLineProviderOptions} for the Git dirty line
 * provider.
 */
final class GitDirtyLineProviderOptions implements DirtyLineProviderOptions {
    private static final String OPTION_PREFIX = "io.github.ssoloff.jedit.plugins.git_dirty_gutter.GitDirtyGutterPlugin."; //$NON-NLS-1$
    private static final String ADDED_CONTENT_COLOR_OPTION = OPTION_PREFIX + "addedContentColor"; //$NON-NLS-1$
    private static final String CHANGED_CONTENT_COLOR_OPTION = OPTION_PREFIX + "changedContentColor"; //$NON-NLS-1$
    private static final String REMOVED_CONTENT_COLOR_OPTION = OPTION_PREFIX + "removedContentColor"; //$NON-NLS-1$

    private static final Color DEFAULT_ADDED_CONTENT_COLOR = Color.GREEN;
    private static final Color DEFAULT_CHANGED_CONTENT_COLOR = Color.ORANGE;
    private static final Color DEFAULT_REMOVED_CONTENT_COLOR = Color.RED;

    private ColorWellButton addedContentColorButton;
    private ColorWellButton changedContentColorButton;
    private ColorWellButton removedContentColorButton;

    private static void addComponent(final JPanel parent, final String labelText, final JComponent component) {
        final JPanel container = new JPanel(new BorderLayout(10, 0));
        container.add(new JLabel(labelText), BorderLayout.CENTER);
        container.add(component, BorderLayout.EAST);
        parent.add(container);
    }

    /*
     * @see lcm.DirtyLineProviderOptions#initOptions(javax.swing.JPanel)
     */
    @Override
    public void initOptions(final JPanel optionPane) {
        optionPane.setLayout(new GridLayout(0, 1));
        addedContentColorButton = new ColorWellButton(loadAddedContentColor());
        addComponent(optionPane, Messages.option_addedContentColorLabel_text(), addedContentColorButton);
        removedContentColorButton = new ColorWellButton(loadRemovedContentColor());
        addComponent(optionPane, Messages.option_removedContentColorLabel_text(), removedContentColorButton);
        changedContentColorButton = new ColorWellButton(loadChangedContentColor());
        addComponent(optionPane, Messages.option_changedContentColorLabel_text(), changedContentColorButton);
    }

    private static Color loadAddedContentColor() {
        return jEdit.getColorProperty(ADDED_CONTENT_COLOR_OPTION, DEFAULT_ADDED_CONTENT_COLOR);
    }

    private static Color loadChangedContentColor() {
        return jEdit.getColorProperty(CHANGED_CONTENT_COLOR_OPTION, DEFAULT_CHANGED_CONTENT_COLOR);
    }

    private static Color loadRemovedContentColor() {
        return jEdit.getColorProperty(REMOVED_CONTENT_COLOR_OPTION, DEFAULT_REMOVED_CONTENT_COLOR);
    }

    private void saveAddedContentColor() {
        jEdit.setColorProperty(ADDED_CONTENT_COLOR_OPTION, addedContentColorButton.getSelectedColor());
    }

    private void saveChangedContentColor() {
        jEdit.setColorProperty(CHANGED_CONTENT_COLOR_OPTION, changedContentColorButton.getSelectedColor());
    }

    /*
     * @see lcm.DirtyLineProviderOptions#saveOptions()
     */
    @Override
    public void saveOptions() {
        saveAddedContentColor();
        saveChangedContentColor();
        saveRemovedContentColor();
    }

    private void saveRemovedContentColor() {
        jEdit.setColorProperty(REMOVED_CONTENT_COLOR_OPTION, removedContentColorButton.getSelectedColor());
    }
}
