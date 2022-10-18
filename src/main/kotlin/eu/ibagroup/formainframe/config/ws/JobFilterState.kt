/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.config.ws

import eu.ibagroup.formainframe.explorer.JesWorkingSet

/**
 * Class to represent a job filter state
 * @param prefix prefix filter field
 * @param owner job owner filter field
 * @param jobId job ID filter field
 */
open class JobFilterState(
  open var prefix: String = "",
  open var owner: String = "",
  open var jobId: String = ""
) : TableRow

/**
 * Job filter extension that stores working set reference
 * @param ws the JES working set for the job filter
 */
class JobFilterStateWithWS(
  var ws: JesWorkingSet,
  override var prefix: String = "*",
  override var owner: String = "*",
  override var jobId: String = ""
) : JobFilterState() {

  /** Transform job filter state to the job filter */
  fun toJobsFilter(): JobsFilter {
    val resultOwner = owner.ifBlank { "" }.uppercase()
    val resultPrefix = prefix.ifBlank { "" }.uppercase()
    val resultJobId = jobId.ifBlank { "" }.uppercase()
    return JobsFilter(resultOwner, resultPrefix, resultJobId)
  }

}
