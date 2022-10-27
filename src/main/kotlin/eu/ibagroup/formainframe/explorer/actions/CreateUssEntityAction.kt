/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.explorer.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runModalTask
import eu.ibagroup.formainframe.analytics.AnalyticsService
import eu.ibagroup.formainframe.analytics.events.FileAction
import eu.ibagroup.formainframe.analytics.events.FileEvent
import eu.ibagroup.formainframe.common.ui.showUntilDone
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.attributes.RemoteUssAttributes
import eu.ibagroup.formainframe.dataops.getAttributesService
import eu.ibagroup.formainframe.dataops.operations.UssAllocationOperation
import eu.ibagroup.formainframe.explorer.ui.*
import eu.ibagroup.formainframe.utils.castOrNull
import eu.ibagroup.formainframe.utils.service
import eu.ibagroup.formainframe.vfs.MFVirtualFile
import eu.ibagroup.r2z.FileType

/**
 * Abstract action for creating Uss Entity (file or directory) through context menu.
 */
abstract class CreateUssEntityAction : AnAction() {

  /**
   * Uss file state which contains parameters for creating.
   */
  abstract val fileType: CreateFileDialogState

  /**
   * Uss file type (file or directory).
   */
  abstract val ussFileType: String

  /**
   * Called when create uss entity is chosen from context menu.
   * Parameters for creation are initialized depending on the entity type.
   * Runs uss allocation operation.
   */
  override fun actionPerformed(e: AnActionEvent) {
    val view = e.getData(FILE_EXPLORER_VIEW) ?: return
    val selected = view.mySelectedNodesData[0]
    val selectedNode = selected.node
    val node = if (selectedNode is UssFileNode) {
      selectedNode.parent?.takeIf { it is UssDirNode }
    } else {
      selectedNode.takeIf { it is UssDirNode }
    } ?: return
    val file = node.virtualFile
    if (node is ExplorerUnitTreeNodeBase<*, *>) {
      val connectionConfig = node.unit.connectionConfig ?: return
      val dataOpsManager = node.unit.explorer.componentManager.service<DataOpsManager>()
      val filePath = if (file != null) {
        dataOpsManager.getAttributesService<RemoteUssAttributes, MFVirtualFile>()
          .getAttributes(file)?.path
      } else {
        (node as UssDirNode).value.path
      }
      if (filePath != null) {
        showUntilDone(
          initialState = fileType.apply { path = filePath },
          { initState -> CreateFileDialog(e.project, state = initState, filePath = filePath) }
        ) {
          var res = false
          val allocationParams = it.toAllocationParams()
          val fileType = if (allocationParams.parameters.type == FileType.FILE) {
            "File"
          } else {
            "Directory"
          }
          runModalTask(
            title = "Creating $fileType ${allocationParams.fileName}",
            project = e.project,
            cancellable = true
          ) {
            runCatching {
              dataOpsManager.performOperation(
                operation = UssAllocationOperation(
                  request = allocationParams,
                  connectionConfig = connectionConfig
                ),
                progressIndicator = it
              )
              val analyticsFileType = if (allocationParams.parameters.type == FileType.FILE)
                eu.ibagroup.formainframe.analytics.events.FileType.USS_FILE
              else eu.ibagroup.formainframe.analytics.events.FileType.USS_DIR

              service<AnalyticsService>().trackAnalyticsEvent(
                FileEvent(
                  allocationParams.parameters.type,
                  FileAction.CREATE
                )
              )
            }.onSuccess {
              node.castOrNull<UssDirNode>()?.cleanCache(false)
              res = true
            }.onFailure { t ->
              view.explorer.reportThrowable(t, e.project)
            }
          }
          res
        }
      }
    }
  }

  override fun isDumbAware(): Boolean {
    return true
  }

  /**
   * Makes action visible only if one node (uss file or uss directory) is selected.
   */
  override fun update(e: AnActionEvent) {
    val view = e.getData(FILE_EXPLORER_VIEW) ?: let {
      e.presentation.isEnabledAndVisible = false
      return
    }
    val selected = view.mySelectedNodesData
    e.presentation.isEnabledAndVisible =
      selected.size == 1 && (selected[0].node is UssDirNode || selected[0].node is UssFileNode)
  }
}
