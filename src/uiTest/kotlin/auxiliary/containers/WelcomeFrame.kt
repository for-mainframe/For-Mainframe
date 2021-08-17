package auxiliary.containers

import auxiliary.clickButton
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun RemoteRobot.welcomeFrame(function: WelcomeFrame.()-> Unit) {
    find(WelcomeFrame::class.java, Duration.ofSeconds(60)).apply(function)
}

@FixtureName("Welcome Frame")
@DefaultXpath("type","//div[@class='FlatWelcomeFrame']")
class WelcomeFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {
    val openProject
        get() = actionLink(byXpath("Open Project", "//div[(@accessiblename='Open or Import' and @class='JButton') or (@class='MainButton' and @text='Open')]"))
    fun open(projectName: String) {
        openProject.click()
        dialog("Open File or Project") {
            textField(byXpath("//div[@class='BorderlessTextField']")).text =
                System.getProperty("user.dir") + "/src/uiTest/resources/$projectName"
            clickButton("OK")
        }
    }
}