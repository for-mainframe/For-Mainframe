/*
 * Copyright (c) 2020-2024 IBA Group.
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

package eu.ibagroup.formainframe.dataops.operations

import com.intellij.openapi.progress.ProgressIndicator
import eu.ibagroup.formainframe.api.ZosmfApi
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.connect.authToken
import eu.ibagroup.formainframe.dataops.attributes.MaskedRequester
import eu.ibagroup.formainframe.dataops.attributes.UssRequester
import eu.ibagroup.formainframe.dataops.exceptions.CallException
import eu.ibagroup.formainframe.testutils.WithApplicationShouldSpec
import eu.ibagroup.formainframe.testutils.testServiceImpl.TestZosmfApiImpl
import eu.ibagroup.formainframe.utils.cancelByIndicator
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.zowe.kotlinsdk.ChangeOwner
import org.zowe.kotlinsdk.DataAPI
import org.zowe.kotlinsdk.FilePath
import retrofit2.Response

class UssChangeOwnerTestSpec : WithApplicationShouldSpec({
  beforeSpec {
    clearAllMocks()
  }

  context("UssChangeOwnerOperationRunner common spec") {

    val dataApi = mockk<DataAPI>()
    val zosmfApi = ZosmfApi.getService() as TestZosmfApiImpl
    zosmfApi.testInstance = object : TestZosmfApiImpl() {
      override fun <Api : Any> getApi(apiClass: Class<out Api>, connectionConfig: ConnectionConfig): Api {
        @Suppress("UNCHECKED_CAST") return dataApi as Api
      }
    }

    val classUnderTest = spyk(UssChangeOwner())
    val operation = mockk<UssChangeOwnerOperation>()

    context("canRun") {

      should("returnTrue_whenCanRun_givenRemoteMemberAttributes") {

        val canRun = classUnderTest.canRun(operation)

        assertSoftly {
          canRun shouldBe true
        }
      }

      should("returnTrue_whenCanRun_givenRemoteDatasetAttributes") {

        val canRun = classUnderTest.canRun(operation)

        assertSoftly {
          canRun shouldBe true
        }
      }

      should("returnTrue_whenCanRun_givenRemoteUssAttributes") {

        val canRun = classUnderTest.canRun(operation)

        assertSoftly {
          canRun shouldBe true
        }
      }
    }

    context("run operation") {

      val progressIndicator = mockk<ProgressIndicator>()
      val datasetRequester = mockk<MaskedRequester>()
      val ussRequester = mockk<UssRequester>()
      val connectionConfig = mockk<ConnectionConfig>()
      val ussChangeOwnerParams = UssChangeOwnerParams(
        ChangeOwner(
          owner = "owner", group = "group"
        ), "attributes/path"
      )

      mockkStatic("eu.ibagroup.formainframe.config.connect.CredentialServiceKt")
      every { connectionConfig.uuid } returns "00000000"
      every { connectionConfig.authToken } returns "TEST_TOKEN"
      every { datasetRequester.connectionConfig } returns connectionConfig
      every { ussRequester.connectionConfig } returns connectionConfig
      every { progressIndicator.checkCanceled() } just Runs
      every { operation.connectionConfig } returns connectionConfig
      every { operation.request } returns ussChangeOwnerParams
      val apiResponse = mockk<Response<Void>>()
      every {
        dataApi.changeFileOwner(any(), any(), any(), any()).cancelByIndicator(progressIndicator).execute()
      } returns apiResponse

      should("successfully run changeFileOwner operation") {
        every { apiResponse.isSuccessful } returns true

        classUnderTest.run(operation, progressIndicator)

        verify(exactly = 1) {
          dataApi.changeFileOwner(
            "TEST_TOKEN", null, ChangeOwner(
              owner = "owner", group = "group"
            ), FilePath("attributes/path")
          )
        }
      }

      should("failed run changeFileOwner operation") {
        every { apiResponse.isSuccessful } returns false
        every { apiResponse.code() } returns 500
        val exception = shouldThrowExactly<CallException> {
          classUnderTest.run(operation, progressIndicator)
        }
        assertSoftly {
          exception.message shouldBe "Cannot change file owner on attributes/path\nCode: 500"
        }
      }

    }


  }
})