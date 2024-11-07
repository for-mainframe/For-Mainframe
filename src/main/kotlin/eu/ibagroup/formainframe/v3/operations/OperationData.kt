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

import eu.ibagroup.formainframe.v3.ConnectionConfig
import eu.ibagroup.formainframe.v3.Requester

/**
 * Interface to describe an operation data
 * @property resultClass the result class of the result that should be returned after an operation execution
 * @property origin the exact operation requester to distinguish the source of the operation request
 */
interface OperationData<Result, ConnectionConfigType : ConnectionConfig> {
  val resultClass: Class<out Result>
  val origin: Requester<ConnectionConfigType>?
}
