package auxiliary.containers

import auxiliary.ClosableCommonContainerFixture
import auxiliary.clickButton
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import java.time.Duration

fun ContainerFixture.addConnectionDialog(
    stack: MutableList<Locator>,
    timeout: Duration = Duration.ofSeconds(60),
    function: AddConnectionDialog.() -> Unit = {}) {
    find<AddConnectionDialog>(AddConnectionDialog.xPath(), timeout).apply {
        stack.add(AddConnectionDialog.xPath())
        function()
        stack.removeLast()
    }
}

@FixtureName("Add Connection Dialog")
open class AddConnectionDialog(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : ClosableCommonContainerFixture(remoteRobot, remoteComponent) {
    val connectionTextParams = findAll<JTextFieldFixture>(byXpath("//div[@class='JBTextField']"))
    fun addConnection(connectionName: String, connectionUrl: String, username: String, password: String, ssl: Boolean) {
        connectionTextParams[0].text = connectionName
        connectionTextParams[1].text = connectionUrl
        connectionTextParams[2].text = username
        textField(byXpath("//div[@class='JPasswordField']")).text = password
        if (ssl) {
            checkBox(byXpath("//div[@accessiblename='Accept self-signed SSL certificates' and @class='JBCheckBox' and @text='Accept self-signed SSL certificates']"))
                .select()
        }
    }
    fun cancel() {
        clickButton("Cancel")
    }
    fun ok() {
        clickButton("OK")
    }
    override fun close() {
        cancel()
    }
    companion object {
        const val name = "Add Connection Dialog"
        @JvmStatic
        fun xPath() = byXpath( name,"//div[@accessiblename='Add Connection' and @class='MyDialog']")
    }
}