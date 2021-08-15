package auxiliary.containers

import auxiliary.ClosableCommonContainerFixture
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun ContainerFixture.editConnectionDialog(
    stack: MutableList<Locator>,
    timeout: Duration = Duration.ofSeconds(60),
    function: EditConnectionDialog.() -> Unit = {}) {
    find<EditConnectionDialog>(EditConnectionDialog.xPath(), timeout).apply {
        stack.add(EditConnectionDialog.xPath())
        function()
        stack.removeLast()
    }
}

class EditConnectionDialog(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : AddConnectionDialog(remoteRobot, remoteComponent) {
    companion object {
        const val name = "Edit Connection Dialog"
        @JvmStatic
        fun xPath() = byXpath( name,"//div[@accessiblename='Edit Connection' and @class='MyDialog']")
    }
}