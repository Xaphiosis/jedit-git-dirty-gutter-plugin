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

import org.gjt.sp.jedit.jEdit;

/**
 * Provides localized messages for the plugin.
 */
@SuppressWarnings("javadoc")
final class Messages {
    private Messages() {
    }

    static String option_addedDirtyMarkColorLabel_text() {
        return jEdit.getProperty("messages.GitDirtyGutterPlugin.option_addedDirtyMarkColorLabel_text"); //$NON-NLS-1$
    }

    static String option_changedDirtyMarkColorLabel_text() {
        return jEdit.getProperty("messages.GitDirtyGutterPlugin.option_changedDirtyMarkColorLabel_text"); //$NON-NLS-1$
    }

    static String option_removedDirtyMarkColorLabel_text() {
        return jEdit.getProperty("messages.GitDirtyGutterPlugin.option_removedDirtyMarkColorLabel_text"); //$NON-NLS-1$
    }
}
