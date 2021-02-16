package eu.ibagroup.formainframe.dataops.api

import com.intellij.openapi.application.ApplicationManager
import eu.ibagroup.formainframe.config.connect.ConnectionConfig

interface ZosmfApi {

  companion object {
    @JvmStatic
    val instance: ZosmfApi = ApplicationManager.getApplication().getService(ZosmfApi::class.java)
  }

  fun <Api : Any> getApi(apiClass: Class<out Api>, connectionConfig: ConnectionConfig): Api

}

inline fun <reified Api : Any> api(connectionConfig: ConnectionConfig): Api {
  return ZosmfApi.instance.getApi(Api::class.java, connectionConfig)
}