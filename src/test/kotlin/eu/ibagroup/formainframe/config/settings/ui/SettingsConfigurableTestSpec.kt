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

package eu.ibagroup.formainframe.config.settings.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import eu.ibagroup.formainframe.analytics.AnalyticsService
import eu.ibagroup.formainframe.analytics.ui.AnalyticsPolicyDialog
import eu.ibagroup.formainframe.config.ConfigService
import eu.ibagroup.formainframe.testutils.WithApplicationShouldSpec
import eu.ibagroup.formainframe.testutils.getPrivateFieldValue
import eu.ibagroup.formainframe.testutils.setPrivateFieldValue
import eu.ibagroup.formainframe.testutils.testServiceImpl.TestAnalyticsServiceImpl
import eu.ibagroup.formainframe.testutils.testServiceImpl.TestConfigServiceImpl
import eu.ibagroup.formainframe.utils.validateJobReturnCode
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JButton
import javax.swing.JLabel

class SettingsConfigurableTestSpec : WithApplicationShouldSpec({
  afterSpec {
    clearAllMocks()
  }

  context("SettingsConfigurable test") {
    val settingsConfigurable = spyk<SettingsConfigurable>(recordPrivateCalls = true)
    val configService = ConfigService.getService() as TestConfigServiceImpl
    val analyticsService = AnalyticsService.getService() as TestAnalyticsServiceImpl
    analyticsService.testInstance = object : TestAnalyticsServiceImpl() {
      override var isAnalyticsEnabled = false
    }

    val panel = settingsConfigurable.createPanel()

    should("Analytics") {
      (panel.getComponent(2) as JLabel).text shouldBe "you haven't agreed to the collection and processing of data"
      mockkConstructor(AnalyticsPolicyDialog::class)
      every { anyConstructed<AnalyticsPolicyDialog>().showAndGet() } returns true
      val analyticsButton = panel.getComponent(1) as JButton
      runBlocking {
        withContext(Dispatchers.EDT) {
          analyticsButton.doClick()
        }
      }
      (panel.getComponent(2) as JLabel).text shouldBe "you have agreed to the collection and processing of data"

      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "agreementLabelComponent", null)
      runBlocking {
        withContext(Dispatchers.EDT) {
          analyticsButton.doClick()
        }
      }
    }

    val successTextField = panel.getComponent(5) as JBTextField
    val warningTextField = panel.getComponent(7) as JBTextField
    val batchTextField = panel.getComponent(11) as JBTextField

    should("isModified") {

      settingsConfigurable.isModified shouldBe false
      val isAutoSyncEnabledField = panel.getComponent(12) as JBCheckBox
      isAutoSyncEnabledField.isSelected = false
      settingsConfigurable.isModified shouldBe true
      settingsConfigurable.reset()
      batchTextField.text = "200"
      settingsConfigurable.isModified shouldBe true
      settingsConfigurable.reset()
      successTextField.text = "2"
      settingsConfigurable.isModified shouldBe true
      settingsConfigurable.reset()
      warningTextField.text = "9"
      settingsConfigurable.isModified shouldBe true
      settingsConfigurable.reset()
    }

    should("Rate Us") {
      var isRateUsOpened = false
      mockkStatic(BrowserUtil::class)
      every { BrowserUtil.browse(any<String>()) } answers {
        isRateUsOpened = true
      }
      val rateUsButton = panel.getComponent(16) as JButton
      runBlocking {
        withContext(Dispatchers.EDT) {
          rateUsButton.doClick()
        }
      }
      isRateUsOpened shouldBe true
    }

    should("Validation") {
      configService.resetTestService()
      successTextField.text = "-1"
      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "isReturnCodesValid", true)
      settingsConfigurable.apply()
      settingsConfigurable.reset()
      getPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "isReturnCodesValid") shouldBe false

      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "panel", null)
      configService.resetTestService()
      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "isReturnCodesValid", true)
      successTextField.text = "2"
      settingsConfigurable.apply()
      settingsConfigurable.reset()
      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "panel", panel)
      getPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "isReturnCodesValid") shouldBe true
    }

    should("JES Explorer") {
      configService.resetTestService()
      successTextField.text = "2"
      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "isReturnCodesValid", true)
      settingsConfigurable.apply()
      configService.resetTestService()
      successTextField.text = "-1"
      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "isReturnCodesValid", false)
      settingsConfigurable.apply()
      configService.isSuccessMinCodeChanged shouldBe false
      configService.isWarningMinCodeeChanged shouldBe false
      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "isReturnCodesValid", true)
      successTextField.text = "2"
      warningTextField.text = "9"
      settingsConfigurable.apply()
      configService.isSuccessMinCodeChanged shouldBe true
      configService.isWarningMinCodeeChanged shouldBe true
      settingsConfigurable.cancel()
      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "panel", null)
      settingsConfigurable.reset()
      setPrivateFieldValue(settingsConfigurable, SettingsConfigurable::class.java, "panel", panel)
    }

    should("validateJobReturnCode") {
      validateJobReturnCode(
        successTextField, AtomicInteger(1), warningTextField, AtomicInteger(8), successTextField
      ) shouldBe null

      successTextField.text = "-1"
      (validateJobReturnCode(
        successTextField, AtomicInteger(1), warningTextField, AtomicInteger(8), successTextField
      ) as ValidationInfo).message shouldBe "Return code should be greater or equal than 0"
      successTextField.text = "1"

      successTextField.text = "T"
      (validateJobReturnCode(
        successTextField, AtomicInteger(-1), warningTextField, AtomicInteger(8), successTextField
      ) as ValidationInfo).message shouldBe "Return code should be greater or equal than 0"
      successTextField.text = "1"

      warningTextField.text = "-1"
      successTextField.text = "-1"
      (validateJobReturnCode(
        successTextField, AtomicInteger(1), warningTextField, AtomicInteger(8), successTextField
      ) as ValidationInfo).message shouldBe "Return code should be greater or equal than 0"
      successTextField.text = "1"

      warningTextField.text = "-1"
      (validateJobReturnCode(
        successTextField, AtomicInteger(1), warningTextField, AtomicInteger(8), successTextField
      ) as ValidationInfo).message shouldBe "Return code should be greater or equal than 0"
      warningTextField.text = "8"

      warningTextField.text = "T"
      (validateJobReturnCode(
        successTextField, AtomicInteger(1), warningTextField, AtomicInteger(-1), successTextField
      ) as ValidationInfo).message shouldBe "Return code should be greater or equal than 0"
      warningTextField.text = "8"

      successTextField.text = "-"
      validateJobReturnCode(
        successTextField, AtomicInteger(1), warningTextField, AtomicInteger(8), successTextField
      ) shouldBe null
      successTextField.text = "1"

      warningTextField.text = "-"
      validateJobReturnCode(
        successTextField, AtomicInteger(1), warningTextField, AtomicInteger(8), successTextField
      ) shouldBe null
      warningTextField.text = "8"

      successTextField.text = "9"
      (validateJobReturnCode(
        successTextField, AtomicInteger(1), warningTextField, AtomicInteger(8), successTextField
      ) as ValidationInfo).message shouldBe "Success return code should be less than warning return code"
      settingsConfigurable.apply()
    }
  }

})