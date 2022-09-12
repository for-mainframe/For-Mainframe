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

import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.dataops.Operation
import eu.ibagroup.r2z.InfoResponse
import eu.ibagroup.r2z.SystemsResponse

/**
 * Base class which represents an operation to be performed against systems info defined to z/OSMF particular instance
 * @param connectionConfig represents a connection config object
 */
class InfoOperation(var connectionConfig: ConnectionConfig) : Operation<SystemsResponse> {
  override val resultClass = SystemsResponse::class.java
}

/**
 * Base class which represents an operation to be performed against system where z/OSMF instance is running
 * @param connectionConfig represents a connection config object
 */
class ZOSInfoOperation(var connectionConfig: ConnectionConfig) : Operation<InfoResponse> {
  override val resultClass = InfoResponse::class.java
}
