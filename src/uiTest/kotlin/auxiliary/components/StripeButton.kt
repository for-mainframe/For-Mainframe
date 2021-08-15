package auxiliary.components

import com.intellij.openapi.wm.impl.StripeButton
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.data.componentAs
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.LambdaLocator
import com.intellij.remoterobot.search.locators.Locator
import java.time.Duration

fun CommonContainerFixture.stripeButton(locator: Locator): StripeButtonFixture {
    return find(locator, Duration.ofSeconds(5))
}

fun CommonContainerFixture.stripeButton(text: String) = stripeButton(StripeButtonFixture.byText(text))

@DefaultXpath(by = "StripeButton type", xpath = "//div[@class='StripeButton']")
@FixtureName("StripeButton")
open class StripeButtonFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : ComponentFixture(remoteRobot, remoteComponent) {
    companion object {
        fun byText(text: String) = LambdaLocator("text '$text") {
            it.isShowing && it is StripeButton && it.text == text
        }
    }
}