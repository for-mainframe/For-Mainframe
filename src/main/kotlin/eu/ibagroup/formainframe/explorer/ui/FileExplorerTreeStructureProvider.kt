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

import eu.ibagroup.formainframe.explorer.ExplorerViewSettings

class FileExplorerTreeStructureProvider : ExplorerTreeStructureProvider() {
  override fun modifyOurs(
    parent: ExplorerTreeNode<*, *>,
    children: Collection<ExplorerTreeNode<*, *>>,
    settings: ExplorerViewSettings
  ): MutableCollection<ExplorerTreeNode<*, *>> {
    return children.toMutableList()
  }
}
