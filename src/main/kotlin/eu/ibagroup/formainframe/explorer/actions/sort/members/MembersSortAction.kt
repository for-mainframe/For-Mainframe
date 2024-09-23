/*
 * Copyright (c) 2020-2024 IBA Group.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   IBA Group
 *   Zowe Community
 */

package eu.ibagroup.formainframe.explorer.actions.sort.members

import com.intellij.openapi.actionSystem.AnActionEvent
import eu.ibagroup.formainframe.dataops.BatchedRemoteQuery
import eu.ibagroup.formainframe.dataops.sort.SortQueryKeys
import eu.ibagroup.formainframe.explorer.actions.sort.SortAction
import eu.ibagroup.formainframe.explorer.ui.ExplorerTreeView
import eu.ibagroup.formainframe.explorer.ui.FileExplorerView
import eu.ibagroup.formainframe.explorer.ui.LibraryNode
import eu.ibagroup.formainframe.explorer.ui.getExplorerView
import eu.ibagroup.formainframe.utils.castOrNull
import eu.ibagroup.formainframe.utils.clearAndMergeWith
import eu.ibagroup.formainframe.utils.clearOldKeysAndAddNew

class MembersSortAction: SortAction<LibraryNode>() {
  override fun getSourceView(e: AnActionEvent): FileExplorerView? {
    return e.getExplorerView()
  }

  override fun getSourceNode(view: ExplorerTreeView<*, *, *>): LibraryNode? {
    return view.mySelectedNodesData[0].node.castOrNull()
  }

  override fun shouldEnableSortKeyForNode(selectedNode: LibraryNode, sortKey: SortQueryKeys): Boolean {
    return selectedNode.currentSortQueryKeysList.contains(sortKey)
  }

  override fun performQueryUpdateForNode(selectedNode: LibraryNode, sortKey: SortQueryKeys) {
    val queryToUpdate = selectedNode.query as BatchedRemoteQuery
    selectedNode.currentSortQueryKeysList.clearOldKeysAndAddNew(sortKey)
    queryToUpdate.sortKeys.clearAndMergeWith(selectedNode.currentSortQueryKeysList)
  }
}
