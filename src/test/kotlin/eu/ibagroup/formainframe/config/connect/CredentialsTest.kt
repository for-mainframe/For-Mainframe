package eu.ibagroup.formainframe.config.connect

import com.intellij.mock.MockApplication
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import eu.ibagroup.formainframe.config.SettingsUnitTestCase
import eu.ibagroup.formainframe.config.connect.ui.ConnectionDialogState
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.lang.Math.abs

internal class CredentialsTest: SettingsUnitTestCase() {

    val connectionDialogState = ConnectionDialogState(connectionName = "a", connectionUrl = "https://a.com", username = "a", password = "a")

    fun testEquals() {

    }

    fun testHashCode() {
        val credentials = Credentials(connectionDialogState.connectionConfig.uuid, connectionDialogState.username, connectionDialogState.password)
        val credentials2 = Credentials(connectionDialogState.connectionConfig.uuid, connectionDialogState.username, connectionDialogState.password)
        assertNotEquals(credentials.hashCode(),credentials2.hashCode())
        assertTrue(credentials.hashCode().toString().length >= 9)
    }

}