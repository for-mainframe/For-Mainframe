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

package eu.ibagroup.formainframe.explorer.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.runModalTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import eu.ibagroup.formainframe.analytics.AnalyticsService
import eu.ibagroup.formainframe.analytics.events.FileAction
import eu.ibagroup.formainframe.analytics.events.FileEvent
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.attributes.*
import eu.ibagroup.formainframe.dataops.content.synchronizer.checkFileForSync
import eu.ibagroup.formainframe.dataops.operations.RenameOperation
import eu.ibagroup.formainframe.explorer.ui.*
import eu.ibagroup.formainframe.telemetry.NotificationsService
import eu.ibagroup.formainframe.v3.operations.OperationsService
import eu.ibagroup.formainframe.v3.operations.RenameOperationData
import eu.ibagroup.formainframe.vfs.MFVirtualFile

typealias ConnectionConfigNew = eu.ibagroup.formainframe.v3.ConnectionConfig
typealias UssRequesterNew = eu.ibagroup.formainframe.v3.UssRequester<ConnectionConfigNew>

/**
 * Class which represents a "Rename" action.
 * The action is shown and triggered only on [UssFileNode], [UssDirNode] (not a USS mask),
 * [LibraryNode] and [FileLikeDatasetNode] node types
 */
class RenameAction : AnAction() {

  override fun getActionUpdateThread() = ActionUpdateThread.EDT

  /**
   * Method to run rename operation. It passes the control to rename operation runner
   * @param project the current project
   * @param view the file explorer view to refresh same nodes elsewhere for
   * @param file the virtual file to be renamed
   * @param type the type of the virtual file to be renamed
   * @param attributes remote attributes of the given virtual file
   * @param newName a new name of the virtual file in VFS
   * @param node an instance of the explorer node
   * @throws any throwable during the processing of the request
   * @return Void
   */
  private fun runRenameOperation(
    project: Project?,
    view: FileExplorerView,
    file: VirtualFile,
    type: String,
    attributes: FileAttributes,
    newName: String,
    node: ExplorerTreeNode<*, *>
  ) {
    runModalTask(
      title = "Renaming $type ${file.name} to $newName",
      project = project,
      cancellable = true
    ) {
      runCatching {
        // TODO: rework
        val originConnectionConfig = if (node is ExplorerUnitTreeNodeBase<*, *, *>) {
          node.unit.connectionConfig
        } else null
        val oldRequester = if (attributes is MFRemoteFileAttributes<*, *>) {
          attributes.requesters.find { it.connectionConfig == originConnectionConfig }
        } else null
        if (oldRequester == null || oldRequester !is UssRequester) {
          DataOpsManager.getService()
            .performOperation(
              operation = RenameOperation(
                file = file,
                attributes = attributes,
                newName = newName
              ),
              progressIndicator = it
            )
        } else {
          val oldConnectionConfig = oldRequester.connectionConfig
          val newConnectionConfig = ConnectionConfigNew(
            oldConnectionConfig.uuid,
            oldConnectionConfig.name,
            oldConnectionConfig.url,
            oldConnectionConfig.isAllowSelfSigned,
            oldConnectionConfig.zVersion,
            oldConnectionConfig.owner
          )
          val newRequester = UssRequesterNew(newConnectionConfig)
          OperationsService.getService()
            .performOperation(
              operationData = RenameOperationData(
                file = file,
                attributes = attributes,
                newName = newName,
                origin = newRequester
              ),
              progressIndicator = it
            )
        }
      }
        .onSuccess {
          val nodesToRefresh = view.myFsTreeStructure.findByValue(node.value)
          nodesToRefresh.forEach {
            it.parent?.cleanCacheIfPossible(cleanBatchedQuery = true)
          }
        }
        .onFailure {
          NotificationsService.errorNotification(it, project)
        }
    }
  }

  /**
   * Run "Rename" action on the selected element.
   * Runs only on [UssFileNode], [UssDirNode] (not a USS mask), [LibraryNode] or [FileLikeDatasetNode] node types
   */
  override fun actionPerformed(e: AnActionEvent) {
    val view = e.getExplorerView<FileExplorerView>() ?: return
    val selectedNodeData = view.mySelectedNodesData[0]
    val node = selectedNodeData.node
    if (
      node is LibraryNode ||
      node is FileLikeDatasetNode ||
      (node is UssDirNode && !node.isUssMask) ||
      node is UssFileNode
    ) {
      val attributes = selectedNodeData.attributes ?: return
      val type: String
      val state: String
      val file: MFVirtualFile?
      when (attributes) {
        is RemoteDatasetAttributes -> {
          type = "Dataset"
          state = attributes.datasetInfo.name
          file = node.virtualFile
        }

        is RemoteMemberAttributes -> {
          type = "Member"
          state = attributes.info.name
          file = node.virtualFile
        }

        is RemoteUssAttributes -> {
          type = if (attributes.isDirectory) "Directory" else "File"
          state = attributes.name
          file = selectedNodeData.file
        }

        else -> {
          throw Exception("Error during rename action execution: Unknown attributes type. $attributes")
        }
      }
      if (file != null) {
        if (checkFileForSync(e.project, file, checkDependentFiles = true)) return
        val dialog = RenameDialog(e.project, type, selectedNodeData, this, state)
        if (dialog.showAndGet()) {
          runRenameOperation(e.project, view, file, type, attributes, dialog.state, node)
          AnalyticsService.getService().trackAnalyticsEvent(FileEvent(attributes, FileAction.RENAME))
        }
      }
    }
  }

  /**
   * Make "Rename" action visible if a context menu is called.
   * The "Rename" action will be visible if:
   * 1. A context menu is triggered in a [FileExplorerView]
   * 2. Selected only one node
   * 3. The node is a [UssFileNode], [UssDirNode] (not a USS mask), [LibraryNode] or [FileLikeDatasetNode]
   * 4. The node is a non-migrated dataset
   */
  override fun update(e: AnActionEvent) {
    val view = e.getExplorerView<FileExplorerView>() ?: let {
      e.presentation.isEnabledAndVisible = false
      return
    }
    val selectedNodesData = view.mySelectedNodesData
    if (selectedNodesData.size != 1) {
      e.presentation.isEnabledAndVisible = false
      return
    }
    val node = selectedNodesData[0].node
    if (
      node !is UssFileNode &&
      (node !is UssDirNode || node.isUssMask) &&
      node !is LibraryNode &&
      node !is FileLikeDatasetNode
    ) {
      e.presentation.isEnabledAndVisible = false
      return
    }
    val file = selectedNodesData[0].file
    if (file != null) {
      val attributes = DataOpsManager.getService().tryToGetAttributes(file) as? RemoteDatasetAttributes
      if (attributes?.hasDsOrg == false) {
        e.presentation.isEnabledAndVisible = false
        return
      }
    }
    e.presentation.isEnabledAndVisible = true
  }

  /**
   * Determines if an action is dumb aware
   */
  override fun isDumbAware(): Boolean {
    return true
  }
}
