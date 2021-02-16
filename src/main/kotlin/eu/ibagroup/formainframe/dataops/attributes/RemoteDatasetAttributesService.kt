package eu.ibagroup.formainframe.dataops.attributes

import com.intellij.openapi.util.io.FileAttributes
import com.intellij.util.SmartList
import eu.ibagroup.formainframe.utils.mergeWith
import eu.ibagroup.formainframe.vfs.MFVirtualFile
import eu.ibagroup.formainframe.vfs.createAttributes
import eu.ibagroup.r2z.Dataset

const val MIGRATED = "Migrated"
const val DATASETS_SUBFOLDER_NAME = "Data Sets"

class RemoteDatasetAttributesService : MFRemoteAttributesServiceBase<RemoteDatasetAttributes>() {

  override val attributesClass = RemoteDatasetAttributes::class.java

  override val subFolderName = DATASETS_SUBFOLDER_NAME

  override fun buildUniqueAttributes(attributes: RemoteDatasetAttributes): RemoteDatasetAttributes {
    return RemoteDatasetAttributes(
      Dataset(
        name = attributes.name,
        volumeSerial = attributes.volser
      ),
      url = attributes.url,
      requesters = SmartList()
    )
  }

  override fun mergeAttributes(oldAttributes: RemoteDatasetAttributes, newAttributes: RemoteDatasetAttributes): RemoteDatasetAttributes {
    return RemoteDatasetAttributes(
      datasetInfo = newAttributes.datasetInfo,
      url = newAttributes.url,
      requesters = oldAttributes.requesters.mergeWith(newAttributes.requesters)
    )
  }

  override fun continuePathChain(attributes: RemoteDatasetAttributes): List<Pair<String, FileAttributes>> {
    return listOf(
      Pair(attributes.volser ?: MIGRATED, createAttributes(directory = true)),
      Pair(attributes.name, createAttributes(directory = attributes.isDirectory))
    )
  }

  override fun reassignAttributesAfterUrlFolderRenaming(
    file: MFVirtualFile,
    urlFolder: MFVirtualFile,
    oldAttributes: RemoteDatasetAttributes,
    newAttributes: RemoteDatasetAttributes
  ) {
    if (oldAttributes.volser != newAttributes.volser) {
      val volserDir = fsModel.findOrCreate(
        this, subDirectory, newAttributes.volser ?: MIGRATED, createAttributes(directory = true)
      )
      fs.moveFile(this, file, volserDir)
    }
    if (oldAttributes.name != newAttributes.name) {
      fs.renameFile(this, file, newAttributes.name)
    }
  }

}