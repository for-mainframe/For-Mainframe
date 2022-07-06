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

import java.util.*

// TODO: doc
class JobsFilter {

  var owner: String = ""

  var prefix: String = ""

  var jobId: String = ""

  var userCorrelatorFilter: String = ""

  constructor()

  constructor(owner: String, prefix: String, jobId: String) {
    this.owner = owner
    this.prefix = prefix
    this.jobId = jobId
  }


  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val filter = other as JobsFilter
    return owner == filter.owner && prefix == filter.prefix && jobId == filter.jobId && userCorrelatorFilter == filter.userCorrelatorFilter
  }

  override fun hashCode(): Int {
    return Objects.hash(owner, prefix, jobId, userCorrelatorFilter)
  }

  override fun toString(): String = if (jobId.isEmpty()) {
    "PREFIX=$prefix OWNER=$owner"
  } else {
    "JobID=$jobId"
  }


}
