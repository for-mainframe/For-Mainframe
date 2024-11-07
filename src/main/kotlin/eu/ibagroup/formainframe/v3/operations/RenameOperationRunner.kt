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

import com.intellij.openapi.progress.ProgressIndicator
import eu.ibagroup.formainframe.api.api
import eu.ibagroup.formainframe.config.connect.authToken
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.attributes.RemoteDatasetAttributes
import eu.ibagroup.formainframe.dataops.attributes.RemoteMemberAttributes
import eu.ibagroup.formainframe.dataops.attributes.RemoteUssAttributes
import eu.ibagroup.formainframe.dataops.attributes.Requester
import eu.ibagroup.formainframe.dataops.exceptions.CallException
import eu.ibagroup.formainframe.utils.cancelByIndicator
import eu.ibagroup.formainframe.utils.runWriteActionInEdtAndWait
import eu.ibagroup.formainframe.v3.ConnectionConfig
import org.zowe.kotlinsdk.DataAPI
import org.zowe.kotlinsdk.FilePath
import org.zowe.kotlinsdk.MoveUssFile
import org.zowe.kotlinsdk.RenameData
import retrofit2.Call

typealias ConnectionConfigOld = eu.ibagroup.formainframe.config.connect.ConnectionConfig
typealias UssRequesterOld = eu.ibagroup.formainframe.dataops.attributes.UssRequester

/** [RenameOperationData] runner */
class RenameOperationRunner<ConnectionConfigType : ConnectionConfig> :
  UnitOperationRunner<ConnectionConfigType, RenameOperationData<ConnectionConfigType>>() {

  override val operationDataClass = RenameOperationData::class.java

  /**
   * Allow operation run only for datasets, members and USS files / folders
   * @see [OperationRunner.canRun]
   */
  override fun canRun(operationData: RenameOperationData<ConnectionConfigType>): Boolean {
    return with(operationData.attributes) {
      this is RemoteMemberAttributes || this is RemoteDatasetAttributes || this is RemoteUssAttributes
    }
  }

  /**
   * Run the [RenameOperationData] with the provided parameters
   * @see [OperationRunner.run]
   */
  override fun run(
    operationData: RenameOperationData<ConnectionConfigType>,
    progressIndicator: ProgressIndicator
  ) {
    when (val attributes = operationData.attributes) {
      is RemoteDatasetAttributes -> {
        // TODO: rework entirely
        // TODO: requesters.forEach - remove
        attributes.requesters.forEach {
          val renameOperationCallBuilder = { connectionConfig: ConnectionConfigOld ->
            api<DataAPI>(connectionConfig).renameDataset(
              authorizationToken = connectionConfig.authToken,
              body = RenameData(
                fromDataset = RenameData.FromDataset(
                  oldDatasetName = attributes.name
                )
              ),
              toDatasetName = operationData.newName
            )
          }
          processRenameOperation(
            operationData,
            progressIndicator,
            it,
            renameOperationCallBuilder,
            "Unable to rename the selected dataset"
          )
        }
      }

      is RemoteMemberAttributes -> {
        // TODO: rework entirely
        val parentAttributes = DataOpsManager.getService()
          .tryToGetAttributes(attributes.parentFile) as RemoteDatasetAttributes
        // TODO: requesters.forEach - remove
        parentAttributes.requesters.forEach {
          val renameOperationCallBuilder = { connectionConfig: ConnectionConfigOld ->
            api<DataAPI>(connectionConfig).renameDatasetMember(
              authorizationToken = connectionConfig.authToken,
              body = RenameData(
                fromDataset = RenameData.FromDataset(
                  oldDatasetName = parentAttributes.datasetInfo.name,
                  oldMemberName = attributes.info.name
                )
              ),
              toDatasetName = parentAttributes.datasetInfo.name,
              memberName = operationData.newName
            )
          }
          processRenameOperation(
            operationData,
            progressIndicator,
            it,
            renameOperationCallBuilder,
            "Unable to rename the selected member"
          )
        }
      }

      is RemoteUssAttributes -> {
        // TODO: rework
        val newRequester = operationData.origin
        val oldRequester = UssRequesterOld(
          ConnectionConfigOld(
            newRequester.connectionConfig.uuid,
            newRequester.connectionConfig.name,
            newRequester.connectionConfig.url,
            newRequester.connectionConfig.isAllowSelfSigned,
            newRequester.connectionConfig.zVersion,
            newRequester.connectionConfig.owner
          )
        )
        val parentDirPath = attributes.parentDirPath
        val renameOperationCallBuilder = { connectionConfig: ConnectionConfigOld ->
          api<DataAPI>(connectionConfig).moveUssFile(
            authorizationToken = connectionConfig.authToken,
            body = MoveUssFile(
              from = attributes.path
            ),
            filePath = FilePath("$parentDirPath/${operationData.newName}")
          )
        }
        processRenameOperation(
          operationData,
          progressIndicator,
          oldRequester,
          renameOperationCallBuilder,
          "Unable to rename the selected file or directory"
        )
      }
    }
  }

  /**
   * Send the rename operation call and rename the respective elements in the virtual file system
   * @param operationData the [RenameOperationData] instance
   * @param progressIndicator the progress indicator to cancel the operation by on the call end
   * @param requester ...
   * @param renameOperationCallBuilder the operation call builder to build the call
   * @param exceptionMsg the exception message to put in the [CallException] if the operation was not successful
   */
  private fun processRenameOperation(
    operationData: RenameOperationData<ConnectionConfigType>,
    progressIndicator: ProgressIndicator,
    requester: Requester<ConnectionConfigOld>,
    renameOperationCallBuilder: (ConnectionConfigOld) -> Call<Void>,
    exceptionMsg: String = "Unable to rename the element"
  ) {
    try {
      progressIndicator.checkCanceled()
      val response = renameOperationCallBuilder(requester.connectionConfig)
        .cancelByIndicator(progressIndicator)
        .execute()
      if (response.isSuccessful) {
        runWriteActionInEdtAndWait {
          operationData.file.rename(this, operationData.newName)
        }
      } else {
        throw CallException(response, exceptionMsg)
      }
    } catch (e: Throwable) {
      if (e is CallException) {
        throw e
      } else {
        throw RuntimeException(e)
      }
    }
  }
}
