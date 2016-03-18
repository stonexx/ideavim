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

package com.maddyhome.idea.vim.extension;

import com.intellij.openapi.application.ApplicationManager;

/**
 * @author vlan
 */
public abstract class VimNonDisposableExtension implements VimExtension {
  private boolean myInitialized = false;

  @Override
  public final void init() {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      initOnce();
    }
    else {
      if (!myInitialized) {
        myInitialized = true;
        initOnce();
      }
    }
  }

  @Override
  public final void dispose() {
  }

  protected abstract void initOnce();
}
