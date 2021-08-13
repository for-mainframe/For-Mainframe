package settings.connection

import com.intellij.openapi.components.service
import com.intellij.testFramework.UsefulTestCase
import customTestCase.PluginTestCase
import eu.ibagroup.formainframe.config.configCrudable
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.connect.Credentials
import eu.ibagroup.formainframe.config.connect.ui.ConnectionConfigurable
import eu.ibagroup.formainframe.config.connect.ui.ConnectionDialogState
import eu.ibagroup.formainframe.config.connect.ui.ConnectionsTableModel
import eu.ibagroup.formainframe.config.sandboxCrudable
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.exceptions.CallException
import eu.ibagroup.formainframe.dataops.operations.InfoOperation
import eu.ibagroup.formainframe.utils.crudable.getAll
import eu.ibagroup.formainframe.utils.crudable.getByForeignKey
import junit.framework.TestCase
import org.junit.jupiter.api.Assertions.assertNotEquals
import java.net.UnknownHostException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.streams.toList

class ConnectionManagerTest: PluginTestCase() {

    private lateinit var conTab: ConnectionsTableModel
    private val conConfig = ConnectionConfigurable()
    private val conState = ConnectionDialogState(
        connectionName = "testConnection",
        connectionUrl = "https://zzow03.zowe.marist.cloud:10443/",
        username = "MENTEE1",
        password = "--------",
        isAllowSsl = true)
    private val conStateA = ConnectionDialogState(connectionName = "a", connectionUrl = "https://a.com", username = "a", password = "a")
    private val conStateB = ConnectionDialogState(connectionName = "b", connectionUrl = "https://b.com", username = "b", password = "b")

    override fun setUp() {
        super.setUp()
        conTab = ConnectionsTableModel(sandboxCrudable)
    }

    override fun tearDown() {
        for (item in conTab.fetch(sandboxCrudable)) {
            conTab.onDelete(sandboxCrudable,item)
        }
        super.tearDown()
    }

    fun assertCrudable(connectionDialogStateList: List<ConnectionDialogState>) {
        var conConfigSet = emptySet<ConnectionConfig>()
        var creSet = emptyList<Credentials>()
        for (connectionDialogState in connectionDialogStateList) {
            conConfigSet += connectionDialogState.connectionConfig
            creSet += connectionDialogState.credentials
        }
        TestCase.assertEquals(conConfigSet.toList(),sandboxCrudable.getAll<ConnectionConfig>().toList())
        TestCase.assertEquals(creSet,sandboxCrudable.getAll<Credentials>().toList())
    }

    fun testOnAdd() {
        conTab.onAdd(sandboxCrudable, conStateA)
        conTab.onAdd(sandboxCrudable, conStateB)
        conConfig.apply()
        UsefulTestCase.assertEquals(
            mutableListOf(conStateA, conStateB),
            conTab.fetch(sandboxCrudable)
        )
        assertCrudable(listOf(conStateA, conStateB))
        conTab.onDelete(sandboxCrudable,conStateA)
        conTab.onDelete(sandboxCrudable,conStateB)
        assertCrudable(listOf())
    }

    fun testOnAddExistingName() {
        val connectionDialogState = ConnectionDialogState(connectionName = conStateA.connectionName)
        conTab.onAdd(sandboxCrudable, conStateA)
        conTab.onAdd(sandboxCrudable, connectionDialogState)
        conConfig.apply()
        UsefulTestCase.assertEquals(mutableListOf(conStateA), conTab.fetch(sandboxCrudable))
        assertCrudable(listOf(conStateA))
        conTab.onDelete(sandboxCrudable,conStateA)
        assertCrudable(listOf())
    }

    fun testOnAddExistingUrl() {
        val connectionDialogState = ConnectionDialogState(connectionUrl = conStateA.connectionUrl)
        conTab.onAdd(sandboxCrudable, conStateA)
        conTab.onAdd(sandboxCrudable, connectionDialogState)
        conConfig.apply()
        UsefulTestCase.assertEquals(
            mutableListOf(conStateA, connectionDialogState),
            conTab.fetch(sandboxCrudable)
        )
        assertCrudable(listOf(conStateA, connectionDialogState))
        conTab.onDelete(sandboxCrudable,conStateA)
        conTab.onDelete(sandboxCrudable,connectionDialogState)
        assertCrudable(listOf())
    }

    fun testSet() {
        conTab.addRow(conStateA)
        conTab.onAdd(sandboxCrudable,conStateA)
        conConfig.apply()
        conTab[0] = conStateB
        conConfig.apply()
        assertEquals(conStateB.connectionName,conTab[0].connectionName)
        assertEquals(conStateB.connectionUrl,conTab[0].connectionUrl)
        assertEquals(conStateB.username,conTab[0].username)
        assertEquals(conStateB.password,conTab[0].password)
        assertNotEquals(conStateB.connectionUuid,conTab[0].connectionUuid)
        assertNotEquals(conStateB.connectionConfig.url,conTab[0].connectionConfig.url)
        assertCrudable(listOf(conStateB))
    }


    fun testConnectionErrors() {
        assertNoThrowable { service<DataOpsManager>().performOperation(InfoOperation(conState.connectionConfig.url, conState.isAllowSsl)) }
        val unknownHostException = org.junit.jupiter.api.assertThrows<UnknownHostException> {
            service<DataOpsManager>().performOperation(InfoOperation("https://a.com", true))
        }
        TestCase.assertEquals("a.com: Name or service not known", unknownHostException.message)
        val callException = org.junit.jupiter.api.assertThrows<CallException> {
            service<DataOpsManager>().performOperation(InfoOperation("https://google.com", true))
        }
        TestCase.assertEquals("Cannot connect to z/OSMF Server\nCode: 404", callException.message)
        val sSLPeerUnverifiedException = org.junit.jupiter.api.assertThrows<SSLPeerUnverifiedException> {
            service<DataOpsManager>().performOperation(InfoOperation(conState.connectionConfig.url, false))
        }
        TestCase.assertEquals("""
              |Hostname zzow03.zowe.marist.cloud not verified:
              |    certificate: sha256/sNWEKME+51NTSsxHGsS8jTcInUoBNkG0HtBU89C/HRg=
              |    DN: CN=ZZOW03.ZOWE.MARIST.CLOUD, OU=IZUDFLT, O=IBM
              |    subjectAltNames: []
              """.trimMargin(), sSLPeerUnverifiedException.message)
    }


}