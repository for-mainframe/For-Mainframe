/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */
package eu.ibagroup.formainframe.dataops.operations.mover

import com.intellij.openapi.progress.ProgressIndicator
import eu.ibagroup.formainframe.api.apiWithBytesConverter
import eu.ibagroup.formainframe.config.connect.authToken
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.attributes.RemoteDatasetAttributes
import eu.ibagroup.formainframe.dataops.attributes.RemoteUssAttributes
import eu.ibagroup.formainframe.dataops.content.synchronizer.DocumentedSyncProvider
import eu.ibagroup.formainframe.dataops.content.synchronizer.addNewLine
import eu.ibagroup.formainframe.dataops.exceptions.CallException
import eu.ibagroup.formainframe.dataops.operations.OperationRunner
import eu.ibagroup.formainframe.dataops.operations.OperationRunnerFactory
import eu.ibagroup.formainframe.utils.applyIfNotNull
import eu.ibagroup.formainframe.utils.cancelByIndicator
import eu.ibagroup.formainframe.utils.castOrNull
import eu.ibagroup.formainframe.utils.runWriteActionInEdtAndWait
import eu.ibagroup.formainframe.vfs.MFVirtualFile
import eu.ibagroup.r2z.DataAPI
import eu.ibagroup.r2z.XIBMDataType

/**
 * Factory for registering CrossSystemUssFileToPdsMover in Intellij IoC container.
 * @see CrossSystemUssFileToPdsMover
 * @author Valiantsin Krus
 */
class CrossSystemUssFileToPdsMoverFactory : OperationRunnerFactory {
  override fun buildComponent(dataOpsManager: DataOpsManager): OperationRunner<*, *> {
    return CrossSystemUssFileToPdsMover(dataOpsManager)
  }
}

/**
 * Implements copying of uss file to partitioned data set between different systems.
 * @author Valiantsin Krus
 */
class CrossSystemUssFileToPdsMover(val dataOpsManager: DataOpsManager) : AbstractFileMover() {

  /**
   * Checks that source is uss file, dest is partitioned data set, and they are located inside different systems.
   * @see OperationRunner.canRun
   */
  override fun canRun(operation: MoveCopyOperation): Boolean {
    return !operation.source.isDirectory &&
            operation.destination.isDirectory &&
            operation.destinationAttributes is RemoteDatasetAttributes &&
            operation.destination is MFVirtualFile &&
            (operation.source !is MFVirtualFile || operation.commonUrls(dataOpsManager).isEmpty())
  }

  /**
   * Proceeds move/copy of uss file to partitioned data set between different systems.
   * @param operation requested operation.
   * @param progressIndicator indicator that will show progress of copying/moving in UI.
   */
  private fun proceedCrossSystemMoveCopy(operation: MoveCopyOperation, progressIndicator: ProgressIndicator): Throwable? {
    var throwable: Throwable? = null
    val sourceFile = operation.source
    val destFile = operation.destination
    val destAttributes = operation.destinationAttributes.castOrNull<RemoteDatasetAttributes>()
      ?: return IllegalStateException("No attributes found for destination directory \'${destFile.name}\'.")

    val destConnectionConfig = destAttributes.requesters.map { it.connectionConfig }.firstOrNull()
      ?: return Throwable("No connection for destination directory found.")

    if (sourceFile is MFVirtualFile) {
      val contentSynchronizer = dataOpsManager.getContentSynchronizer(sourceFile)
      val syncProvider = DocumentedSyncProvider(sourceFile)
      contentSynchronizer?.synchronizeWithRemote(syncProvider, progressIndicator)
    }

    var memberName = sourceFile.name.filter { it.isLetterOrDigit() }.take(8)
    if (memberName.isEmpty()) {
      memberName = "empty"
    }

    val xIBMDataType = if (sourceFile.fileType.isBinary ||
      (operation.sourceAttributes.castOrNull<RemoteUssAttributes>()?.contentMode?.type == XIBMDataType.Type.BINARY)
    ) XIBMDataType(XIBMDataType.Type.BINARY)
    else XIBMDataType(XIBMDataType.Type.TEXT)

    val sourceContent = sourceFile.contentsToByteArray()
    val contentToUpload =
      if (sourceFile.fileType.isBinary) sourceContent else sourceContent.filter { it != '\r'.code.toByte() }
        .toByteArray()

    val response = apiWithBytesConverter<DataAPI>(destConnectionConfig).writeToDatasetMember(
      authorizationToken = destConnectionConfig.authToken,
      datasetName = destAttributes.name,
      memberName = memberName,
      content = contentToUpload.addNewLine(),
      xIBMDataType = xIBMDataType
    ).applyIfNotNull(progressIndicator) { indicator ->
      cancelByIndicator(indicator)
    }.execute()

    if (!response.isSuccessful &&
      response.errorBody()?.string()?.contains("Truncation of a record occurred during an I/O operation.") != true
    ) {
      throwable = CallException(response, "Cannot upload data to '${destAttributes.name}(${memberName})'")
    } else {
      destFile.children.firstOrNull { it.name.uppercase() == memberName.uppercase() }?.let { file ->
        runWriteActionInEdtAndWait {
          val syncProvider = DocumentedSyncProvider(file, { _, _, _ -> false }, { th -> throwable = th })
          val contentSynchronizer = dataOpsManager.getContentSynchronizer(file)

          if (contentSynchronizer == null) {
            throwable = IllegalArgumentException("Cannot get content synchronizer for file '${file.name}'")
          } else {
            contentSynchronizer.synchronizeWithRemote(syncProvider, progressIndicator)
          }
        }
      }
    }

    return throwable
  }

  /**
   * Starts operation execution. Throws throwable if something went wrong.
   * @throws Throwable
   * @see OperationRunner.run
   */
  override fun run(operation: MoveCopyOperation, progressIndicator: ProgressIndicator) {
    val throwable: Throwable? = try {
      proceedCrossSystemMoveCopy(operation, progressIndicator)
    } catch (t: Throwable) {
      t
    }
    if (throwable != null) {
      throw throwable
    }
  }
}