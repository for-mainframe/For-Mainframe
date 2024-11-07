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
import eu.ibagroup.formainframe.v3.ConnectionConfig

/**
 * Abstract class to represent unit operation runner
 * @property resultClass the result class of the operation, that is [Unit]
 */
abstract class UnitOperationRunner<C : ConnectionConfig, O : UnitOperationData<C>> : OperationRunner<Unit, C, O>() {

  override val resultClass = Unit::class.java

  abstract override fun run(operationData: O, progressIndicator: ProgressIndicator)

}
