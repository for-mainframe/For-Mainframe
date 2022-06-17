/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.explorer.ui

import com.intellij.ide.dnd.DnDEvent
import com.intellij.ide.dnd.DnDNativeTarget
import com.intellij.ide.dnd.FileCopyPasteUtil
import com.intellij.ide.dnd.TransferableWrapper
import com.intellij.ide.projectView.impl.ProjectViewImpl
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.awt.RelativeRectangle
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ArrayUtilRt
import eu.ibagroup.formainframe.common.ui.getVirtualFile
import eu.ibagroup.formainframe.common.ui.makeNodeDataFromTreePath
import eu.ibagroup.formainframe.explorer.Explorer
import groovy.lang.Tuple4
import java.awt.Rectangle
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

val IS_DRAG_AND_DROP_KEY = DataKey.create<Boolean>("IsDropKey")
val DRAGGED_FROM_PROJECT_FILES_ARRAY = DataKey.create<List<VirtualFile>>("DraggedFilesFromProject")

class GlobalExplorerViewDropTarget(
  val myTree: Tree,
  val explorer: Explorer<*>,
  private val copyPasteSupport: GlobalFileExplorerView.ExplorerCopyPasteSupport
) : DnDNativeTarget {

  override fun drop(event: DnDEvent) {
    val sourcesTargetBounds = getSourcesTargetAndBounds(event) ?: return

//    val pasteProvider = copyPasteSupport.getPasteProvider(listOf(sourcesTargetBounds.second))
    val pasteProvider = copyPasteSupport.pasteProvider
    val cutProvider = copyPasteSupport.cutProvider
    val sourceTreePaths = sourcesTargetBounds.first?.toList() ?: listOf()
    val copyCutContext = DataContext {
      when (it) {
        CommonDataKeys.PROJECT.name -> copyPasteSupport.project
        ExplorerDataKeys.NODE_DATA_ARRAY.name -> sourceTreePaths
          .mapNotNull { treePath ->
            if ((treePath?.lastPathComponent as DefaultMutableTreeNode).userObject is ExplorerTreeNode<*>)
              makeNodeDataFromTreePath(explorer, treePath)
            else null
          }.toTypedArray()
        CommonDataKeys.VIRTUAL_FILE_ARRAY.name -> {
          if (sourcesTargetBounds.fourth == myTree) {
            arrayOf(makeNodeDataFromTreePath(explorer, sourcesTargetBounds.second).file)
          } else {
            arrayOf(sourcesTargetBounds.second.getVirtualFile())
          }
        }
        IS_DRAG_AND_DROP_KEY.name -> true
        DRAGGED_FROM_PROJECT_FILES_ARRAY.name -> {
          if (event.attachedObject is GlobalExplorerViewDragSource.ExplorerTransferableWrapper) {
            emptyList()
          } else {
            sourcesTargetBounds.first?.mapNotNull { treePath -> treePath?.getVirtualFile() }
          }
        }
        else -> null
      }
    }
    if (cutProvider.isCutEnabled(copyCutContext)) {
      cutProvider.performCut(copyCutContext)
    }
    pasteProvider.performPaste(copyCutContext)
  }

  fun getProjectTree (): JTree? {
    return copyPasteSupport.project?.let { ProjectViewImpl.getInstance(it).currentProjectViewPane.tree }
  }

  override fun update(event: DnDEvent): Boolean {
    val sourcesTargetBounds = getSourcesTargetAndBounds(event) ?: return false
    val sources = sourcesTargetBounds.first ?: return false
    if (ArrayUtilRt.find(sources, sourcesTargetBounds.second) != -1 || !FileCopyPasteUtil.isFileListFlavorAvailable(event)) {
      return false
    }

//    val pasteEnabled = copyPasteSupport.isPastePossibleFromPath(listOf(sourcesTargetBounds.second), sources.toList())
//    val pasteEnabled = false
    val isCopiedFromRemote = event.attachedObject is GlobalExplorerViewDragSource.ExplorerTransferableWrapper

    val pasteEnabled = if (isCopiedFromRemote && sourcesTargetBounds.fourth == myTree)
      copyPasteSupport.isPastePossibleFromPath(listOf(sourcesTargetBounds.second), sources.toList())
    else if (isCopiedFromRemote && sourcesTargetBounds.fourth === getProjectTree()) {
      val vFile = sourcesTargetBounds.second.getVirtualFile()
      if (vFile == null) {
        false
      } else {
        copyPasteSupport.isPastePossible(listOf(vFile), sources.map { makeNodeDataFromTreePath(explorer, it) })
      }
    } else if (!isCopiedFromRemote && sourcesTargetBounds.fourth == myTree) {
      val sourceFiles = sources.mapNotNull { it?.getVirtualFile() }
      val target = makeNodeDataFromTreePath(explorer, sourcesTargetBounds.second).file?.let { listOf(it) } ?: emptyList()
      copyPasteSupport.isPastePossibleForFiles(target, sourceFiles)
    } else false

    event.isDropPossible = pasteEnabled
    if (pasteEnabled) {
      event.setHighlighting(
        RelativeRectangle(sourcesTargetBounds.fourth, sourcesTargetBounds.third),
        DnDEvent.DropTargetHighlightingType.RECTANGLE
      )
    }
    return false
  }

  private fun getSourcesTargetAndBounds(event: DnDEvent): Tuple4<Array<TreePath?>?, TreePath, Rectangle, JTree>? {
    event.setDropPossible(false, "")

    val point = event.point ?: return null

    val treeToUpdate: JTree

    val projectTree = getProjectTree()
    treeToUpdate = if (event.currentOverComponent == myTree) {
      myTree
    } else if (event.currentOverComponent == projectTree && projectTree != null) {
      projectTree
    } else {
      return null
    }
    val target: TreePath = treeToUpdate.getClosestPathForLocation(point.x, point.y) ?: return null


    val bounds = treeToUpdate.getPathBounds(target)
    if (bounds == null || bounds.y > point.y || point.y >= bounds.y + bounds.height) return null

    val sources = getSourcePaths(event.attachedObject)
    return Tuple4(sources, target, bounds, treeToUpdate)
  }

  private fun getSourcePaths(transferData: Any): Array<TreePath?>? {
    val wrapper = if (transferData is TransferableWrapper) transferData else null
    return wrapper?.treePaths
  }
}
