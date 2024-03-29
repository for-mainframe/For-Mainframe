/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.dataops

import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.utils.UNIT_CLASS

/**
 * Class which represents Unit type of the Result of remote query
 */
data class UnitRemoteQueryImpl<R>(
  override val request: R,
  override val connectionConfig: ConnectionConfig
) : RemoteQuery<R, Unit> {
  override val resultClass: Class<out Unit>
    get() = UNIT_CLASS
}