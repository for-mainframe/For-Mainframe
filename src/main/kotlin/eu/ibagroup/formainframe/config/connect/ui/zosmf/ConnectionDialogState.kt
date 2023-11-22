/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.config.connect.ui.zosmf

import eu.ibagroup.formainframe.common.ui.DialogMode
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.connect.Credentials
import eu.ibagroup.formainframe.config.connect.ui.ConnectionDialogStateBase
import eu.ibagroup.formainframe.utils.crudable.Crudable
import eu.ibagroup.formainframe.utils.crudable.getByUniqueKey
import eu.ibagroup.formainframe.utils.crudable.nextUniqueValue
import org.zowe.kotlinsdk.annotations.ZVersion

/**
 * Data class which represents state for connection dialog
 */
data class ConnectionDialogState(
  override var connectionUuid: String = "",
  override var connectionName: String = "",
  override var connectionUrl: String = "",
  /*var apiMeditationLayer: String = "",*/
  override var username: String = "",
  override var password: String = "",
  var isAllowSsl: Boolean = false,
  var zVersion: ZVersion = ZVersion.ZOS_2_1,
  var owner: String = "",
  override var mode: DialogMode = DialogMode.CREATE
) : ConnectionDialogStateBase<ConnectionConfig>() {

  override var connectionConfig
    get() = ConnectionConfig(connectionUuid, connectionName, connectionUrl, isAllowSsl, zVersion, owner)
    set(value) {
      connectionUuid = value.uuid
      connectionName = value.name
      connectionUrl = value.url
      isAllowSsl = value.isAllowSelfSigned
      zVersion = value.zVersion
      owner = value.owner
    }


  override var credentials
    get() = Credentials(connectionUuid, username, password)
    set(value) {
      username = value.username
      password = value.password
    }

  public override fun clone(): ConnectionDialogState {
    return ConnectionDialogState(
      connectionUuid = connectionUuid,
      connectionName = connectionName,
      connectionUrl = connectionUrl,
      username = username,
      password = password,
      isAllowSsl = isAllowSsl,
      owner = owner
    )
  }
}

fun ConnectionDialogState.initEmptyUuids(crudable: Crudable): ConnectionDialogState {
  this.connectionUuid = crudable.nextUniqueValue<ConnectionConfig, String>()
  return this
}

fun ConnectionConfig.toDialogState(crudable: Crudable): ConnectionDialogState {

  val credentials = crudable.getByUniqueKey<Credentials>(this.uuid) ?: Credentials().apply {
    this.configUuid = this@toDialogState.uuid
  }
  return ConnectionDialogState(
    connectionUuid = this.uuid,
    connectionName = this.name,
    connectionUrl = this.url,
    username = credentials.username,
    password = credentials.password,
    isAllowSsl = this.isAllowSelfSigned,
    zVersion = this.zVersion,
    owner = this.owner
  )
}