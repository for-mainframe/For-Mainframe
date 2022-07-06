/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.dataops.content.adapters

import com.intellij.openapi.vfs.VirtualFile
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.attributes.FileAttributes
import eu.ibagroup.formainframe.utils.`is`

// TODO: doc Valiantsin
abstract class MFContentAdapterBase<Attributes : FileAttributes>(
  protected val dataOpsManager: DataOpsManager
) : MFContentAdapter {
  abstract val vFileClass: Class<out VirtualFile>
  abstract val attributesClass: Class<out Attributes>


  override fun accepts(file: VirtualFile): Boolean {
    val fileAttributesClass = dataOpsManager.tryToGetAttributes(file)?.javaClass ?: return false
    return vFileClass.isAssignableFrom(file::class.java) &&
            attributesClass.isAssignableFrom(fileAttributesClass)
  }

  abstract fun adaptContentToMainframe(content: ByteArray, attributes: Attributes): ByteArray

  @Suppress("UNCHECKED_CAST")
  override fun prepareContentToMainframe(content: ByteArray, file: VirtualFile): ByteArray {
    val attributes = dataOpsManager.tryToGetAttributes(file) ?: return content
    return if (attributes.`is`(attributesClass)) adaptContentToMainframe(content, attributes as Attributes) else content
  }

  abstract fun adaptContentFromMainframe(content: ByteArray, attributes: Attributes): ByteArray

  @Suppress("UNCHECKED_CAST")
  override fun adaptContentFromMainframe(content: ByteArray, file: VirtualFile): ByteArray {
    val attributes = dataOpsManager.tryToGetAttributes(file) ?: return content
    return if (attributes.`is`(attributesClass)) adaptContentFromMainframe(
      content,
      attributes as Attributes
    ) else content
  }
}
