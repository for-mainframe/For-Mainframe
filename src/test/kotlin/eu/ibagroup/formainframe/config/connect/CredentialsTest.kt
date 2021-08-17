package eu.ibagroup.formainframe.config.connect

import eu.ibagroup.formainframe.config.UnitTestCase
import eu.ibagroup.formainframe.config.connect.ui.ConnectionDialogState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CredentialsTest: UnitTestCase() {
    val connectionDialogState = ConnectionDialogState(connectionName = "a", connectionUrl = "https://a.com", username = "a", password = "a")

    @Test
    fun testHashCode() {
        val credentials = Credentials(connectionDialogState.connectionConfig.uuid, connectionDialogState.username, connectionDialogState.password)
        val credentials2 = Credentials(connectionDialogState.connectionConfig.uuid, connectionDialogState.username, connectionDialogState.password)
        assertNotEquals(credentials.hashCode(),credentials2.hashCode())
    }
}