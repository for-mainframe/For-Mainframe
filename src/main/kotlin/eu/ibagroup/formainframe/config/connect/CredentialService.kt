/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.config.connect

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import eu.ibagroup.formainframe.dataops.exceptions.CredentialsNotFoundForConnection
import okhttp3.Credentials

// TODO: doc
fun interface CredentialsListener {
  fun onChanged(connectionConfigUuid: String)
}

@JvmField
val CREDENTIALS_CHANGED = Topic.create("credentialChanges", CredentialsListener::class.java)

interface CredentialService {

  fun getUsernameByKey(connectionConfigUuid: String): String?
  fun getPasswordByKey(connectionConfigUuid: String): String?
  fun setCredentials(connectionConfigUuid: String, username: String, password: String)
  fun clearCredentials(connectionConfigUuid: String)

  companion object {
    @JvmStatic
    val instance: CredentialService
      get() = ApplicationManager.getApplication().getService(CredentialService::class.java)
  }

}

fun getInstance(): CredentialService = ApplicationManager.getApplication().getService(CredentialService::class.java)

fun username(connectionConfig: ConnectionConfig): String {
  return CredentialService.instance.getUsernameByKey(connectionConfig.uuid) ?: throw CredentialsNotFoundForConnection(
    connectionConfig
  )
}

fun password(connectionConfig: ConnectionConfig): String {
  return CredentialService.instance.getPasswordByKey(connectionConfig.uuid) ?: throw CredentialsNotFoundForConnection(
    connectionConfig
  )
}

val ConnectionConfig.authToken: String
  get() = Credentials.basic(username(this), password(this))