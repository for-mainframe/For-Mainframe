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
import eu.ibagroup.formainframe.api.api
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.connect.authToken
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.attributes.RemoteDatasetAttributes
import eu.ibagroup.formainframe.dataops.attributes.RemoteUssAttributes
import eu.ibagroup.formainframe.dataops.exceptions.CallException
import eu.ibagroup.formainframe.dataops.operations.OperationRunner
import eu.ibagroup.formainframe.dataops.operations.OperationRunnerFactory
import eu.ibagroup.formainframe.utils.cancelByIndicator
import eu.ibagroup.formainframe.utils.getParentsChain
import eu.ibagroup.r2z.*

/**
 * Factory for registering UssFileToPdsMover in Intellij IoC container.
 * @see UssFileToPdsMover
 * @author Valiantsin Krus
 */
class UssFileToPdsMoverFactory : OperationRunnerFactory {
  override fun buildComponent(dataOpsManager: DataOpsManager): OperationRunner<*, *> {
    return UssFileToPdsMover(dataOpsManager)
  }
}

/**
 * Implements copying of uss file to uss directory inside 1 system.
 * @author Viktar Mushtsin
 */
class UssFileToPdsMover(private val dataOpsManager: DataOpsManager) : AbstractFileMover() {

  /**
   * Checks that source is uss file, destination is partitioned data set,
   * and source and destination are located within the same system.
   * @see OperationRunner.canRun
   */
  override fun canRun(operation: MoveCopyOperation): Boolean {
    return operation.sourceAttributes is RemoteUssAttributes
      && !operation.sourceAttributes.isDirectory
      && operation.destinationAttributes is RemoteDatasetAttributes
      && operation.destinationAttributes.isDirectory
      && operation.commonUrls(dataOpsManager).isNotEmpty()
      && !operation.destination.getParentsChain().containsAll(operation.source.getParentsChain())
  }

  /**
   * Proceeds move/copy of uss file to partitioned data set.
   * @param connectionConfig connection configuration of system inside which to copy file.
   * @param operation requested operation.
   * @param progressIndicator indicator that will show progress of copying/moving in UI.
   */
  private fun proceedMoveCopyToPds(
    connectionConfig: ConnectionConfig,
    operation: MoveCopyOperation,
    progressIndicator: ProgressIndicator
  ): Throwable? {
    val sourceAttributes = (operation.sourceAttributes as RemoteUssAttributes)
    val destinationAttributes = (operation.destinationAttributes as RemoteDatasetAttributes)
    val from = sourceAttributes.path
    val to = destinationAttributes.name
    val api = api<DataAPI>(connectionConfig)
    var memberName = sourceAttributes.name.filter { it.isLetterOrDigit() }.take(8)
    if (memberName.isEmpty()) {
      memberName = "empty"
    }
    val opName = if (operation.isMove) "move" else "copy"
    var throwable: Throwable? = null

    val copyResponse = api.copyToDatasetMemberFromUssFile(
      authorizationToken = connectionConfig.authToken,
      xIBMBpxkAutoCvt = XIBMBpxkAutoCvt.ON,
      body = CopyDataZOS.CopyFromFile(
        request = "copy",
        file = CopyDataZOS.CopyFromFile.File(sourceAttributes.path, CopyDataZOS.CopyFromFile.File.CopyType.TEXT),
        replace = true
      ),
      toDatasetName = destinationAttributes.name,
      memberName = memberName
    ).cancelByIndicator(progressIndicator).execute()
    if (!copyResponse.isSuccessful &&
      copyResponse.errorBody()?.string()?.contains("Truncation of a record occurred during an I/O operation.") != true
    ) {
      return CallException(copyResponse, "Cannot $opName $from to $to")
    }

    if (operation.isMove) {
      val deleteResponse = api.deleteUssFile(
        authorizationToken = connectionConfig.authToken,
        filePath = FilePath(from),
        xIBMOption = XIBMOption.RECURSIVE
      ).execute()
      if (!deleteResponse.isSuccessful) {
        val rollbackResponse = api.deleteDatasetMember(
          authorizationToken = connectionConfig.authToken,
          datasetName = to, memberName = memberName
        ).execute()
        throwable = if (rollbackResponse.isSuccessful) {
          CallException(deleteResponse, "Cannot $opName $from to $to. Rollback proceeded successfully.")
        } else {
          CallException(deleteResponse, "Cannot $opName $from to $to. Rollback failed.")
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
    var throwable: Throwable? = null
    for ((requester, _) in operation.commonUrls(dataOpsManager)) {
      try {
        throwable = proceedMoveCopyToPds(requester.connectionConfig, operation, progressIndicator)
        break
      } catch (t: Throwable) {
        throwable = t
      }
    }
    if (throwable != null) {
      throw throwable
    }
  }


}
