/*
 * Copyright (c) 2024 IBA Group.
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

package eu.ibagroup.formainframe.v3.operations

import com.intellij.openapi.vfs.VirtualFile
import eu.ibagroup.formainframe.dataops.attributes.FileAttributes
import eu.ibagroup.formainframe.v3.ConnectionConfig
import eu.ibagroup.formainframe.v3.Requester

/**
 * Class that represents a rename operation data
 * @param file the virtual file to rename
 * @param attributes the virtual file's attributes
 * @param newName the new name to apply to the virtual file
 */
data class RenameOperationData<ConnectionConfigType : ConnectionConfig>(
  val file: VirtualFile,
  val attributes: FileAttributes,
  val newName: String,
  override val origin: Requester<ConnectionConfigType>
) : UnitOperationData<ConnectionConfigType>
