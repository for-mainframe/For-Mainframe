package eu.ibagroup.formainframe.config.connect.ui

import eu.ibagroup.formainframe.config.ConfigSandboxImpl
import eu.ibagroup.formainframe.config.SettingsUnitTestCase
import kotlin.test.assertNotEquals

class ConnectionsTableModelTest: SettingsUnitTestCase() {
    val sandbox = ConfigSandboxImpl()
    val conTab = ConnectionsTableModel(sandbox.crudable)
    val connectionDialogStateA = ConnectionDialogState(connectionName = "a", connectionUrl = "https://a.com", username = "a", password = "a")
    val connectionDialogStateB = ConnectionDialogState(connectionName = "b", connectionUrl = "https://b.com", username = "b", password = "b")

    fun testFetch() {
        conTab.addRow(connectionDialogStateA)
        assertEquals(mutableListOf(connectionDialogStateA),conTab.fetch(sandbox.crudable))
    }

    fun testOnAdd() {
        conTab.onAdd(sandbox.crudable, connectionDialogStateA)
        conTab.onAdd(sandbox.crudable, connectionDialogStateB)
        assertEquals(mutableListOf(connectionDialogStateA,connectionDialogStateB),conTab.fetch(sandbox.crudable))
    }

    fun testOnAddExistingName() {
        val connectionDialogState = ConnectionDialogState(connectionName = connectionDialogStateA.connectionName)
        conTab.onAdd(sandbox.crudable, connectionDialogStateA)
        conTab.onAdd(sandbox.crudable, connectionDialogState)
        assertEquals(mutableListOf(connectionDialogStateA),conTab.fetch(sandbox.crudable))
    }

    fun testOnAddExistingUrl() {
        val connectionDialogState = ConnectionDialogState(connectionUrl = connectionDialogStateA.connectionUrl)
        conTab.onAdd(sandbox.crudable, connectionDialogStateA)
        conTab.onAdd(sandbox.crudable, connectionDialogState)
        assertEquals(mutableListOf(connectionDialogStateA,connectionDialogState),conTab.fetch(sandbox.crudable))
    }

    fun testOnDelete() {
        conTab.onAdd(sandbox.crudable, connectionDialogStateA)
        conTab.onDelete(sandbox.crudable, connectionDialogStateA)
        assertEquals(mutableListOf<ConnectionDialogState>(),conTab.fetch(sandbox.crudable))
    }

    fun testSet() {
        conTab.addRow(ConnectionDialogState())
        conTab[0] = connectionDialogStateA
        assertEquals(connectionDialogStateA.connectionName,conTab[0].connectionName)
        assertEquals(connectionDialogStateA.connectionUrl,conTab[0].connectionUrl)
        assertEquals(connectionDialogStateA.username,conTab[0].username)
        assertEquals(connectionDialogStateA.password,conTab[0].password)
        assertNotEquals(connectionDialogStateA.connectionUuid,conTab[0].connectionUuid)
    }
}