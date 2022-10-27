/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.utils

import com.ibm.mq.headers.CCSID
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.components.service
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.attributes.RemoteUssAttributes
import eu.ibagroup.formainframe.dataops.content.synchronizer.DEFAULT_BINARY_CHARSET
import eu.ibagroup.formainframe.dataops.operations.uss.ChangeFileTagOperation
import eu.ibagroup.formainframe.dataops.operations.uss.ChangeFileTagOperationParams
import eu.ibagroup.r2z.FileTagList
import eu.ibagroup.r2z.TagAction
import eu.ibagroup.r2z.UssFileDataType
import okhttp3.ResponseBody
import okhttp3.internal.indexOfNonWhitespace
import java.nio.charset.Charset

const val FILE_TAG_NOTIFICATION_GROUP_ID = "eu.ibagroup.formainframe.utils.FileTagNotificationGroupId"

/**
 * Checks if the uss file tag is set.
 * @param attributes uss file attributes.
 */
fun checkUssFileTag(attributes: RemoteUssAttributes) {
  val charset = getUssFileTagCharset(attributes)
  if (charset != null) {
    attributes.ussFileEncoding = charset
  } else {
    attributes.ussFileEncoding = DEFAULT_BINARY_CHARSET
  }
}

/**
 * Get encoding from file tag or return null if it doesn't exist.
 * @param attributes uss file attributes.
 */
fun getUssFileTagCharset(attributes: RemoteUssAttributes): Charset? {
  val responseBody = listUssFileTag(attributes)
  val body = responseBody?.string()
  if (body?.isNotEmpty() == true) {
    val stdout = eu.ibagroup.r2z.gson.fromJson(body, FileTagList::class.java).stdout[0]
    if (stdout.indexOf("t ") > -1) {
      val startPos = stdout.indexOfNonWhitespace(1)
      val endPos = stdout.indexOf(' ', startPos)
      val tagCharset = stdout.substring(startPos, endPos)
      val ccsid = CCSID.getCCSID(tagCharset)
      val codePage = CCSID.getCodepage(ccsid)
      return Charset.forName(codePage)
    }
  }
  return null
}

/**
 * Sets or removes uss file encoding to the file tag.
 * @param attributes uss file attributes.
 */
fun updateFileTag(attributes: RemoteUssAttributes) {
  if (attributes.ussFileEncoding == DEFAULT_BINARY_CHARSET) {
    removeUssFileTag(attributes)
  } else {
    setUssFileTag(attributes)
  }
}

/**
 * Lists the contents of the uss file tag.
 * @param attributes uss file attributes.
 * @return response body or null if empty.
 */
fun listUssFileTag(attributes: RemoteUssAttributes): ResponseBody? {
  var response: ResponseBody? = null

  runCatching {
    val connectionConfig = attributes.requesters[0].connectionConfig
    response = service<DataOpsManager>().performOperation(
      operation = ChangeFileTagOperation(
        request = ChangeFileTagOperationParams(
          filePath = attributes.path,
          action = TagAction.LIST,
        ),
        connectionConfig = connectionConfig
      ),
    )
  }.onFailure {
    notifyError(it,"Cannot list uss file tag for ${attributes.path}")
  }
  return response
}

/**
 * Sets the file tag to the current uss file encoding.
 * @param attributes uss file attributes.
 */
fun setUssFileTag(attributes: RemoteUssAttributes) {
  val ccsid = CCSID.getCCSID(attributes.ussFileEncoding.name())
  val codeSet = ccsid.toString()

  runCatching {
    val connectionConfig = attributes.requesters[0].connectionConfig
    service<DataOpsManager>().performOperation(
      operation = ChangeFileTagOperation(
        request = ChangeFileTagOperationParams(
          filePath = attributes.path,
          action = TagAction.SET,
          type = UssFileDataType.TEXT,
          codeSet = codeSet
        ),
        connectionConfig = connectionConfig
      )
    )
  }.onFailure {
    notifyError(it, "Cannot set uss file tag for ${attributes.path}")
  }
}

/**
 * Removes the uss file tag value.
 * @param attributes uss file attributes.
 */
fun removeUssFileTag(attributes: RemoteUssAttributes) {
  runCatching {
    val connectionConfig = attributes.requesters[0].connectionConfig
    service<DataOpsManager>().performOperation(
      operation = ChangeFileTagOperation(
        request = ChangeFileTagOperationParams(
          filePath = attributes.path,
          action = TagAction.REMOVE,
        ),
        connectionConfig = connectionConfig
      )
    )
  }.onFailure {
    notifyError(it, "Cannot remove uss file tag for ${attributes.path}")
  }
}

/**
 * Displays an error notification if an error was received.
 * @param th thrown error.
 * @param title error text.
 */
private fun notifyError(th: Throwable, title: String) {
  Notifications.Bus.notify(
    Notification(
      FILE_TAG_NOTIFICATION_GROUP_ID,
      title,
      th.message ?: "",
      NotificationType.ERROR
    )
  )
}