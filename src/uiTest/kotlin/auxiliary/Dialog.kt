package auxiliary

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import java.time.Duration

fun ContainerFixture.addConnectionDialog(
    name: String,
    url: String,
    username: String,
    password: String,
    ssl: Boolean,
    function: DialogFixture.() -> Unit = {}) {
    dialog("Add Connection") {
        var conParams = findAll<JTextFieldFixture>(byXpath("//div[@class='JBTextField']"))
        conParams[0].text = name
        conParams[1].text = url
        conParams[2].text = username
        textField(byXpath("//div[@class='JPasswordField']")).text = password
        if (ssl)
        {
            checkBox(byXpath("//div[@accessiblename='Accept self-signed SSL certificates' and @class='JBCheckBox' and @text='Accept self-signed SSL certificates']"))
                .select()
        }
        function()
    }
}

fun ContainerFixture.dialog(
    title: String,
    timeout: Duration = Duration.ofSeconds(60),
    function: DialogFixture.() -> Unit = {}): DialogFixture = step("Search for dialog with title $title") {
    find<DialogFixture>(DialogFixture.byTitle(title), timeout).apply(function)
}

/**
 * this class was copied from ui-robot at jet brains
 */
@FixtureName("Dialog")
class DialogFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    companion object {
        @JvmStatic
        fun byTitle(title: String) = byXpath("title $title", "//div[@title='$title' and @class='MyDialog']")
    }

    val title: String
        get() = callJs("component.getTitle();")
}