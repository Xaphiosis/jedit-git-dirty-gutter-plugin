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
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lcm.DirtyLineProviderOptions;
import org.gjt.sp.jedit.gui.ColorWellButton;

/**
 * Implementation of {@link DirtyLineProviderOptions} for the Git dirty line
 * provider.
 */
final class GitDirtyLineProviderOptions implements DirtyLineProviderOptions {
    private ColorWellButton addedDirtyMarkColorButton;
    private ColorWellButton changedDirtyMarkColorButton;
    private ColorWellButton removedDirtyMarkColorButton;

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
        addedDirtyMarkColorButton = new ColorWellButton(Properties.getAddedDirtyMarkColor());
        addComponent(optionPane, Messages.option_addedDirtyMarkColorLabel_text(), addedDirtyMarkColorButton);
        removedDirtyMarkColorButton = new ColorWellButton(Properties.getRemovedDirtyMarkColor());
        addComponent(optionPane, Messages.option_removedDirtyMarkColorLabel_text(), removedDirtyMarkColorButton);
        changedDirtyMarkColorButton = new ColorWellButton(Properties.getChangedDirtyMarkColor());
        addComponent(optionPane, Messages.option_changedDirtyMarkColorLabel_text(), changedDirtyMarkColorButton);
    }

    /*
     * @see lcm.DirtyLineProviderOptions#saveOptions()
     */
    @Override
    public void saveOptions() {
        Properties.setAddedDirtyMarkColor(addedDirtyMarkColorButton.getSelectedColor());
        Properties.setChangedDirtyMarkColor(changedDirtyMarkColorButton.getSelectedColor());
        Properties.setRemovedDirtyMarkColor(removedDirtyMarkColorButton.getSelectedColor());
    }
}
