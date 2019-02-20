/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2016 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.action.motion.scroll;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.command.Command;
import com.maddyhome.idea.vim.group.MotionGroup;
import com.maddyhome.idea.vim.handler.EditorActionHandlerBase;
import com.maddyhome.idea.vim.helper.EditorHelper;
import org.jetbrains.annotations.NotNull;

/**
 */
public class MotionScrollLastScreenLinePageStartAction extends EditorAction {
  public MotionScrollLastScreenLinePageStartAction() {
    super(new Handler());
  }

  private static class Handler extends EditorActionHandlerBase {
    protected boolean execute(@NotNull Editor editor, @NotNull DataContext context, @NotNull Command cmd) {

      final MotionGroup motion = VimPlugin.getMotion();

      int line = cmd.getRawCount();
      if (line == 0) {
        final int prevVisualLine = EditorHelper.getVisualLineAtTopOfScreen(editor) - 1;
        line = EditorHelper.visualLineToLogicalLine(editor, prevVisualLine) + 1;  // rawCount is 1 based
        return motion.scrollLineToLastScreenLine(editor, line, true);
      }

      // [count]z^ first scrolls [count] to the bottom of the window, then moves the caret to the line that is now at
      // the top, and then move that line to the bottom of the window
      line = EditorHelper.normalizeLine(editor, line);
      if (motion.scrollLineToLastScreenLine(editor, line, true)) {

        line = EditorHelper.getVisualLineAtTopOfScreen(editor);
        line = EditorHelper.visualLineToLogicalLine(editor, line) + 1;  // rawCount is 1 based
        return motion.scrollLineToLastScreenLine(editor, line, true);
      }
      return false;
    }
  }
}
