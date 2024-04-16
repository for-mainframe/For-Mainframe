/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.explorer.actions.sort.uss

import com.intellij.openapi.actionSystem.AnActionEvent
import eu.ibagroup.formainframe.explorer.actions.sort.SortActionGroup
import eu.ibagroup.formainframe.explorer.ui.*

/**
 * Represents the custom USS files sort action group in the FileExplorerView context menu
 */
class UssSortActionGroup : SortActionGroup() {
  override fun getSourceView(e: AnActionEvent): FileExplorerView? {
    return e.getExplorerView()
  }

  override fun checkNode(node: ExplorerTreeNode<*, *>): Boolean {
    return node is UssDirNode
  }
}