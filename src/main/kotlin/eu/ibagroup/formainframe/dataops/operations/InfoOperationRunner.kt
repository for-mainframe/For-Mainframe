/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.dataops.operations

import com.intellij.openapi.progress.ProgressIndicator
import eu.ibagroup.formainframe.api.api
import eu.ibagroup.formainframe.config.connect.authToken
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.exceptions.CallException
import eu.ibagroup.formainframe.utils.cancelByIndicator
import eu.ibagroup.formainframe.utils.log
import org.zowe.kotlinsdk.SystemsApi
import org.zowe.kotlinsdk.SystemsResponse

/**
 * Class which represents factory for info operation runner. Defined in plugin.xml
 */
class InfoOperationRunnerFactory : OperationRunnerFactory {
  override fun buildComponent(dataOpsManager: DataOpsManager): OperationRunner<*, *> {
    return InfoOperationRunner()
  }
}

/**
 * Class which represents info operation runner
 */
class InfoOperationRunner : OperationRunner<InfoOperation, SystemsResponse> {
  override val operationClass = InfoOperation::class.java
  override val resultClass = SystemsResponse::class.java
  override val log = log<InfoOperationRunner>()

  /**
   * Determined if operation can be run on selected object
   * @param operation - specifies an info operation object
   */
  override fun canRun(operation: InfoOperation) = true

  /**
   * Runs an info operation
   * @param operation - info operation to be run
   * @param progressIndicator - progress indicator object
   * @throws CallException if request is not successful or no response body
   * @return SystemsResponse serialized object (body of the request)
   */
  override fun run(operation: InfoOperation, progressIndicator: ProgressIndicator): SystemsResponse {
    val response = api<SystemsApi>(connectionConfig = operation.connectionConfig)
      .getSystems(operation.connectionConfig.authToken)
      .cancelByIndicator(progressIndicator)
      .execute()
    if (!response.isSuccessful) {
      val headMessage = when (response.message()) {
        "Unauthorized" -> "Credentials are not valid"
        "Not Found" -> "Endpoint not found"
        else -> response.message()
      }
      throw CallException(response, headMessage)
    }
    return response.body() ?: throw CallException(response, "Cannot parse z/OSMF info request body")
  }

}