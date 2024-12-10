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

package eu.ibagroup.formainframe.dataops

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.ProjectManager
import eu.ibagroup.formainframe.dataops.content.synchronizer.checkForSync
import eu.ibagroup.formainframe.testutils.WithApplicationShouldSpec
import eu.ibagroup.formainframe.vfs.MFVirtualFile
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.*

class DataOpsManagerImplTestSpec : WithApplicationShouldSpec({

  afterSpec {
    clearAllMocks()
  }

  context("DataOpsMangerImpl test spec") {
    val dataOpsManager = DataOpsManagerImpl()

    var fileClosed = false

    val projectManagerMock = mockk<ProjectManager>()
    every { projectManagerMock.openProjects } returns arrayOf(mockk())

    val fileEditorManagerMock = mockk<FileEditorManager>()

    beforeEach {
      mockkStatic(ProjectManager::getInstance)
      every { ProjectManager.getInstance() } returns projectManagerMock

      every { fileEditorManagerMock.openFiles } returns arrayOf(mockk<MFVirtualFile>())
      fileClosed = false
      every { fileEditorManagerMock.closeFile(any()) } answers {
        fileClosed = true
      }

      mockkStatic(FileEditorManager::getInstance)
      every { FileEditorManager.getInstance(any()) } returns fileEditorManagerMock

      mockkStatic(::checkForSync)
      every { checkForSync(any()) } returns false
    }

    afterEach {
      unmockkAll()
    }

    // DataOpsMangerImpl.clearFileCache
    should("clear the file cache with closing the files in the editor") {
      val result = dataOpsManager.clearFileCache()

      assertSoftly {
        result shouldBe true
        fileClosed shouldBe true
      }
    }
    should("clear the file cache without closing the files in the editor") {
      every { fileEditorManagerMock.openFiles } returns arrayOf(mockk())

      val result = dataOpsManager.clearFileCache()

      assertSoftly {
        result shouldBe true
        fileClosed shouldBe false
      }
    }
    should("do not clear the file cache because the files are synchronized") {
      every { checkForSync(any()) } returns true

      val result = dataOpsManager.clearFileCache()

      assertSoftly {
        result shouldBe false
        fileClosed shouldBe true
      }
    }
  }
})