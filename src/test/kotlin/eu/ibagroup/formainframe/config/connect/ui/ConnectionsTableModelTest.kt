package eu.ibagroup.formainframe.config.connect.ui

import com.intellij.mock.MockApplication
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import eu.ibagroup.formainframe.config.ConfigSandboxImpl
import eu.ibagroup.formainframe.config.UnitTestCase
import io.mockk.spyk
import org.junit.Before
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals

class ConnectionsTableModelTest: UnitTestCase() {
    val sandbox = ConfigSandboxImpl()
    val conTab = ConnectionsTableModel(sandbox.crudable)
    val connectionDialogStateA = ConnectionDialogState(connectionName = "a", connectionUrl = "https://a.com", username = "a", password = "a")
    val connectionDialogStateB = ConnectionDialogState(connectionName = "b", connectionUrl = "https://b.com", username = "b", password = "b")

    @Test
    fun fetch() {
        conTab.addRow(connectionDialogStateA)
        assertEquals(mutableListOf(connectionDialogStateA),conTab.fetch(sandbox.crudable))
    }

    @Test
    fun onAdd() {
        conTab.onAdd(sandbox.crudable, connectionDialogStateA)
        conTab.onAdd(sandbox.crudable, connectionDialogStateB)
        assertEquals(mutableListOf(connectionDialogStateA,connectionDialogStateB),conTab.fetch(sandbox.crudable))
    }

    @Test
    fun onAddExistingName() {
        val connectionDialogState = ConnectionDialogState(connectionName = connectionDialogStateA.connectionName)
        conTab.onAdd(sandbox.crudable, connectionDialogStateA)
        conTab.onAdd(sandbox.crudable, connectionDialogState)
        assertEquals(mutableListOf(connectionDialogStateA),conTab.fetch(sandbox.crudable))
    }

    @Test
    fun onAddExistingUrl() {
        val connectionDialogState = ConnectionDialogState(connectionUrl = connectionDialogStateA.connectionUrl)
        conTab.onAdd(sandbox.crudable, connectionDialogStateA)
        conTab.onAdd(sandbox.crudable, connectionDialogState)
        assertEquals(mutableListOf(connectionDialogStateA,connectionDialogState),conTab.fetch(sandbox.crudable))
    }

    @Test
    fun onDelete() {
        conTab.onAdd(sandbox.crudable, connectionDialogStateA)
        conTab.onDelete(sandbox.crudable, connectionDialogStateA)
        assertEquals(mutableListOf<ConnectionDialogState>(),conTab.fetch(sandbox.crudable))
    }

    @Test
    fun set() {
        conTab.addRow(ConnectionDialogState())
        conTab[0] = connectionDialogStateA
        assertEquals(connectionDialogStateA.connectionName,conTab[0].connectionName)
        assertEquals(connectionDialogStateA.connectionUrl,conTab[0].connectionUrl)
        assertEquals(connectionDialogStateA.username,conTab[0].username)
        assertEquals(connectionDialogStateA.password,conTab[0].password)
        assertNotEquals(connectionDialogStateA.connectionUuid,conTab[0].connectionUuid)
    }

    @Test
    fun shouldFail() {
        assertTrue(false)
    }
}