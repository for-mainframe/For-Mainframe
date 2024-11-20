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

package eu.ibagroup.formainframe.dataops.content.adapters

import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.testutils.WithApplicationShouldSpec
import eu.ibagroup.formainframe.testutils.testServiceImpl.TestDataOpsManagerImpl
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.unmockkAll

class DefaultContentAdapterTestSpec: WithApplicationShouldSpec({

  afterSpec {
    clearAllMocks()
    unmockkAll()
  }

  context("content/adapters: DefaultContentAdapter") {
    val dataOpsManager = DataOpsManager.getService() as TestDataOpsManagerImpl
    val classUnderTest = spyk(DefaultContentAdapter(dataOpsManager), "DefaultContentAdapter")

    should("shouldReturnAdaptedContent_whenAdaptWhitespaces_givenContentToAdapt") {
      val contentToAdapt = "This is a test string.\n   Content should be replaced by   \n" +
          "this content without trailing               \n" +
          "\n" +
          "   whitespaces..."
      val expected = "This is a test string.\n   Content should be replaced by   \n" +
          "this content without trailing               \n" +
          "\n" +
          "   whitespaces..."
      val adaptedContent = classUnderTest.adaptWhitespaces(contentToAdapt)

      assertSoftly {
        adaptedContent shouldBe expected
      }
    }
  }
})