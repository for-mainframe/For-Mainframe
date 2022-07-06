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

import com.intellij.ide.util.treeView.AbstractTreeStructureBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import eu.ibagroup.formainframe.explorer.Explorer
import eu.ibagroup.formainframe.explorer.ExplorerViewSettings

// TODO: doc, is it a Model class?
abstract class ExplorerTreeStructureBase(
  protected val explorer: Explorer<*>,
  protected val project: Project
) : AbstractTreeStructureBase(project), ExplorerViewSettings {

  abstract fun registerNode(node: ExplorerTreeNode<*>)

  abstract fun <V : Any> findByValue(value: V): Collection<ExplorerTreeNode<V>>

  abstract fun findByPredicate(predicate: (ExplorerTreeNode<*>) -> Boolean): Collection<ExplorerTreeNode<*>>

  abstract fun findByVirtualFile(file: VirtualFile): Collection<ExplorerTreeNode<*>>

}
