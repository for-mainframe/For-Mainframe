package auxiliary

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.utils.waitFor
import java.time.Duration

fun CommonContainerFixture.clickButton(text: String) {
    button(text).clickWhenEnabled()
}

fun CommonContainerFixture.clickButton(locator: Locator) {
    button(locator).clickWhenEnabled()
}

fun CommonContainerFixture.clickActionButton(locator: Locator) {
    val button = actionButton(locator)
    waitFor(Duration.ofSeconds(5)) {
        button.isEnabled()
    }
    button.click()
}

