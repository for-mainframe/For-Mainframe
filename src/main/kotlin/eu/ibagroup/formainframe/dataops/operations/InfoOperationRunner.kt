package eu.ibagroup.formainframe.dataops.operations

import com.intellij.openapi.progress.ProgressIndicator
import eu.ibagroup.formainframe.api.api
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.exceptions.CallException
import eu.ibagroup.formainframe.utils.cancelByIndicator
import eu.ibagroup.r2z.InfoAPI
import eu.ibagroup.r2z.InfoResponse

class InfoOperationRunnerFactory : OperationRunnerFactory {
  override fun buildComponent(dataOpsManager: DataOpsManager): OperationRunner<*, *> {
    return InfoOperationRunner()
  }
}

class InfoOperationRunner : OperationRunner<InfoOperation, InfoResponse> {
  override val operationClass = InfoOperation::class.java
  override val resultClass = InfoResponse::class.java

  override fun canRun(operation: InfoOperation) = true

  override fun run(operation: InfoOperation, progressIndicator: ProgressIndicator): InfoResponse {
    val response = api<InfoAPI>(url = operation.url, isAllowSelfSigned = operation.isAllowSelfSigned)
      .getSystemInfo()
      .cancelByIndicator(progressIndicator)
      .execute()
    if (!response.isSuccessful) {
      throw CallException(response, "Cannot connect to z/OSMF Server")
    }
    return response.body() ?: throw CallException(response, "Cannot parse z/OSMF info request body")
  }
}
