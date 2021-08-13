package settings.connection
//
//import customTestCase.PluginTestCase
//import eu.ibagroup.formainframe.config.connect.ConnectionConfig
//import eu.ibagroup.formainframe.config.connect.ui.ConnectionConfigurable
//import eu.ibagroup.formainframe.config.connect.ui.ConnectionDialogState
//import eu.ibagroup.formainframe.config.connect.ui.ConnectionsTableModel
//import eu.ibagroup.formainframe.config.sandboxCrudable
//import eu.ibagroup.formainframe.dataops.operations.DatasetAllocationOperation
//import eu.ibagroup.formainframe.dataops.operations.DatasetAllocationParams
//import eu.ibagroup.formainframe.dataops.operations.DatasetAllocator
//import eu.ibagroup.formainframe.utils.crudable.getAll
//import eu.ibagroup.formainframe.utils.toMutableList
//import eu.ibagroup.r2z.CreateDataset
//import eu.ibagroup.r2z.DatasetOrganization
//import eu.ibagroup.r2z.RecordFormat
//import junit.framework.TestCase
//
//class connectionTestFirst: PluginTestCase() {
//
//    private val conState = ConnectionDialogState(
//        connectionName = "testConnection",
//        connectionUrl = "https://zzow03.zowe.marist.cloud:10443/",
//        username = "MENTEE1",
//        password = "-------",
//        isAllowSsl = true)
//    lateinit var conTab: ConnectionsTableModel
//
//    override fun setUp() {
//        super.setUp()
//        conTab = ConnectionsTableModel(sandboxCrudable)
//        conTab.onAdd(sandboxCrudable, conState)
//        ConnectionConfigurable().apply()
//    }
//
//    fun testDatasetAllocation() {
//
//        val config = sandboxCrudable.getAll<ConnectionConfig>().toMutableList()[0]
//        val url = sandboxCrudable.getAll<UrlConnection>().toMutableList()[0]
//        val name = "TESTC"
//        val param = DatasetAllocationParams(name, CreateDataset(
//            primaryAllocation = 1,
//            secondaryAllocation = 1,
//            recordFormat = RecordFormat.FB,
//            datasetOrganization = DatasetOrganization.PS))
//        val dataOp = DatasetAllocationOperation(param, config, url)
//        DatasetAllocator().run(dataOp)
//    }
//
//    fun testNullEditor() {
//        TestCase.assertNotNull(myFixture.editor)
//    }
//}