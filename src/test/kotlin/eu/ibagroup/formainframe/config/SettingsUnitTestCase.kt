package eu.ibagroup.formainframe.config

import com.intellij.mock.MockApplication
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.UsefulTestCase
import eu.ibagroup.formainframe.config.connect.ui.ConnectionDialogState
import eu.ibagroup.formainframe.config.connect.ui.ConnectionsTableModel
import eu.ibagroup.formainframe.config.ws.DSMask
import eu.ibagroup.formainframe.config.ws.UssPath
import eu.ibagroup.formainframe.config.ws.WorkingSetConfig
import eu.ibagroup.formainframe.config.ws.ui.WSTableModel
import io.mockk.spyk
import org.junit.AfterClass
import org.junit.BeforeClass

abstract class SettingsUnitTestCase: UsefulTestCase() {
    val app = spyk(MockApplication(Disposer.newDisposable("")))

    override fun setUp() {
        ApplicationManager.setApplication(app,Disposer.newDisposable(""))
    }

    override fun tearDown() {
        app.dispose()
    }
}