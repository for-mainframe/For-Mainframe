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
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

open class UnitTestCase {
    val app = spyk(MockApplication(Disposer.newDisposable("")))

    @BeforeEach
    fun setUp() {
        ApplicationManager.setApplication(app,Disposer.newDisposable(""))
    }

    @AfterEach
    fun tearDown() {
        app.dispose()
    }
}