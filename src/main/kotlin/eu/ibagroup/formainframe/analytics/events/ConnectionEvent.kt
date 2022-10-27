/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.analytics.events

/**
 * CRUD event for connections. Uses "connections" as the name of the event.
 * @see CrudAnalyticsEvent
 * @author Valiantsin Krus
 */
class ConnectionEvent(
  override var actionType: ActionType
) : CrudAnalyticsEvent("connections", actionType)