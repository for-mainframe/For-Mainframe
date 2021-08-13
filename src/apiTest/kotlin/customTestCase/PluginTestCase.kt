package customTestCase

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.ibagroup.formainframe.analytics.AnalyticsService
import eu.ibagroup.formainframe.analytics.AnalyticsStartupActivity
import io.mockk.every
import io.mockk.mockkConstructor

abstract class PluginTestCase : BasePlatformTestCase() {

    override fun setUp() {
        mockkConstructor(AnalyticsStartupActivity::class)
        every { AnalyticsStartupActivity().runActivity(any()) } returns Unit

        super.setUp()

        val analyticsService = service<AnalyticsService>()
        analyticsService.isAnalyticsEnabled = true
        analyticsService.isUserAcknowledged = true
    }

    override fun getBasePath() = "/testData/"

    override fun getTestDataPath() = System.getProperty("user.dir") + basePath
}