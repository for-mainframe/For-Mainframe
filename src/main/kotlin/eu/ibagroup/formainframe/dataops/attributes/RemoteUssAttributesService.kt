/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.dataops.attributes

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.SmartList
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.utils.mergeWith
import eu.ibagroup.formainframe.vfs.MFVirtualFile
import eu.ibagroup.formainframe.vfs.createAttributes
import eu.ibagroup.r2z.XIBMDataType

class RemoteUssAttributesServiceFactory : AttributesServiceFactory {
  override fun buildComponent(dataOpsManager: DataOpsManager): AttributesService<*, *> {
    return RemoteUssAttributesService(dataOpsManager)
  }
}

class RemoteUssAttributesService(
  dataOpsManager: DataOpsManager
) : MFRemoteAttributesServiceBase<RemoteUssAttributes>(dataOpsManager) {

  override val attributesClass = RemoteUssAttributes::class.java

  override val subFolderName = "USS"

  override fun buildUniqueAttributes(attributes: RemoteUssAttributes): RemoteUssAttributes {
    return RemoteUssAttributes(
      path = attributes.path,
      isDirectory = attributes.isDirectory,
      fileMode = null,
      url = attributes.url,
      requesters = SmartList(),
      length = 0L,
      uid = attributes.uid,
      owner = attributes.owner,
      gid = attributes.gid,
      groupId = attributes.groupId,
      symlinkTarget = null
    )
  }

  override fun mergeAttributes(
    oldAttributes: RemoteUssAttributes,
    newAttributes: RemoteUssAttributes
  ): RemoteUssAttributes {
    return RemoteUssAttributes(
      path = newAttributes.path,
      isDirectory = newAttributes.isDirectory,
      fileMode = newAttributes.fileMode,
      url = newAttributes.url,
      requesters = oldAttributes.requesters.mergeWith(newAttributes.requesters),
      length = newAttributes.length,
      uid = newAttributes.uid,
      owner = newAttributes.owner,
      gid = newAttributes.gid,
      groupId = newAttributes.groupId,
      modificationTime = newAttributes.modificationTime,
      symlinkTarget = newAttributes.symlinkTarget
    )
  }

  override fun reassignAttributesAfterUrlFolderRenaming(
    file: MFVirtualFile,
    urlFolder: MFVirtualFile,
    oldAttributes: RemoteUssAttributes,
    newAttributes: RemoteUssAttributes
  ) {
    fsModel.setWritable(file, newAttributes.isWritable)
    file.isReadable = newAttributes.isReadable
    if (oldAttributes.name != newAttributes.name) {
      fsModel.renameFile(this, file, newAttributes.name)
    }
    if (oldAttributes.parentDirPath != newAttributes.parentDirPath) {
      var current = subDirectory
      createPathChain(newAttributes).dropLast(1).map { nameWithFileAttr ->
        findOrCreate(current, nameWithFileAttr).also { current = it }
      }
      fsModel.moveFile(this, file, current)
    }
  }

  fun updateWritableFlagAfterContentChanged(vFile: VirtualFile, attributes: RemoteUssAttributes) {
    if(attributes.contentMode.type == XIBMDataType.Type.BINARY) {
      vFile.isWritable = false
    } else {
      vFile.isWritable = attributes.isWritable
    }
  }

  override fun continuePathChain(attributes: RemoteUssAttributes): List<PathElementSeed> {
    return if (attributes.path != "/") {
      val pathTokens = attributes.path.substring(1).split("/")
      pathTokens.dropLast(1).map { PathElementSeed(it, createAttributes(directory = true)) }.plus(
        listOf(
          PathElementSeed(
            name = pathTokens.last(),
            fileAttributes = createAttributes(directory = attributes.isDirectory, writable = attributes.isWritable),
            postCreateAction = {
              isReadable = attributes.isReadable
            }
          )
        )
      )
    } else {
      listOf()
    }
  }
}