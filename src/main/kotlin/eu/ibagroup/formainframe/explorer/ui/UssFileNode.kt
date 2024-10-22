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

package eu.ibagroup.formainframe.explorer.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.IconUtil
import eu.ibagroup.formainframe.common.message
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.attributes.RemoteUssAttributes
import eu.ibagroup.formainframe.dataops.getAttributesService
import eu.ibagroup.formainframe.dataops.sort.SortQueryKeys
import eu.ibagroup.formainframe.explorer.ExplorerUnit
import eu.ibagroup.formainframe.utils.append
import eu.ibagroup.formainframe.utils.toHumanReadableFormat
import eu.ibagroup.formainframe.vfs.MFVirtualFile
import java.time.LocalDateTime

/** USS file representation in the explorer tree */
class UssFileNode(
  file: MFVirtualFile,
  project: Project,
  parent: ExplorerTreeNode<ConnectionConfig, *>,
  unit: ExplorerUnit<ConnectionConfig>,
  treeStructure: ExplorerTreeStructureBase,
  override val currentSortQueryKeysList: List<SortQueryKeys> = mutableListOf(),
  override val sortedNodes: List<AbstractTreeNode<*>> = mutableListOf()
) : ExplorerUnitTreeNodeBase<ConnectionConfig, MFVirtualFile, ExplorerUnit<ConnectionConfig>>(
  file, project, parent, unit, treeStructure
), UssNode {

  override fun update(presentation: PresentationData) {
    updateNodeTitleUsingCutBuffer(value.presentableName, presentation)
    val icon = IconUtil.computeFileIcon(value, Iconable.ICON_FLAG_READ_STATUS, explorer.nullableProject)

    if (this.navigating) {
      presentation.setIcon(AnimatedIcon.Default())
    } else {
      presentation.setIcon(icon)
    }

    val attributes = attributesService.getAttributes(value)
    attributes?.modificationTime?.let {
      presentation
        .append(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        .append(
          message("explorer.tree.node.label.modified", LocalDateTime.parse(it).toHumanReadableFormat()),
          SimpleTextAttributes.GRAY_ATTRIBUTES
        )
    }
    presentation.tooltip = message(
      "explorer.tree.uss.node.tooltip",
      attributes?.owner ?: "NULL", attributes?.fileMode ?: "NULL"
    )
  }

  override fun getVirtualFile(): MFVirtualFile {
    return value
  }

  override fun getChildren(): MutableCollection<out AbstractTreeNode<*>> {
    return mutableListOf()
  }

  private val attributesService
    get() = DataOpsManager.getService().getAttributesService<RemoteUssAttributes, MFVirtualFile>()
}

