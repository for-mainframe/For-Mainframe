/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package auxiliary.containers

import auxiliary.ClosableCommonContainerFixture
import auxiliary.clickButton
import auxiliary.components.tabLabel
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

/**
 * Class representing the Member Properties Dialog.
 */
@FixtureName("Member Properties Dialog")
open class MemberPropertiesDialog(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : ClosableCommonContainerFixture(remoteRobot, remoteComponent) {

    /**
     * Checks if the provided member properties and the properties in opened Member Properties Dialog are matching.
     */
    fun validateMemberProperties(
        generalParams: List<String>,
        dataParams: List<String>): Boolean {
        var result = true
        tabLabel(remoteRobot, "General").click()
        findAll<JTextFieldFixture>(byXpath("//div[@class='JBTextField']")).forEachIndexed { index, field ->
            if(field.text != (generalParams[index])){
                result = false
            }
        }

        tabLabel(remoteRobot, "Data").click()
        findAll<JTextFieldFixture>(byXpath("//div[@class='JBTextField']")).forEachIndexed { index, field ->
            if(field.text != (dataParams[index])){
                result = false
            }
        }


        return result
    }

    fun areMemberPropertiesValid(
        memName: String,
        version: String,
        createDate: String,
        modDate: String,
        modTime: String,
        userId: String,
        curRecNum: String,
        begRecNum: String,
        changedRecNum: String): Boolean {
        var result = true
        val memberGeneralParams = findAll<JTextFieldFixture>(byXpath("//div[@class='JBTextField']"))
        if(memberGeneralParams[0].text != memName || memberGeneralParams[1].text != version ||
            memberGeneralParams[2].text != createDate || memberGeneralParams[3].text != modDate ||
            memberGeneralParams[4].text != modTime || memberGeneralParams[5].text != userId){
            result = false
        }
        Thread.sleep(1000)
        find<ComponentFixture>(byXpath("//div[@text='Data']")).click()
        Thread.sleep(1000)
        val memberDataParams = findAll<JTextFieldFixture>(byXpath("//div[@class='JBTextField']"))
        if(memberDataParams[0].text != curRecNum || memberDataParams[1].text != begRecNum || memberDataParams[2].text != changedRecNum){
            result = false
        }

        return result
    }

    /**
     * The close function, which is used to close the dialog in the tear down method.
     */
    override fun close() {
        clickButton("Cancel")
    }

    companion object {
        const val name = "Member Properties Dialog"

        /**
         * Returns the xPath of the Member Properties Dialog.
         */
        @JvmStatic
        fun xPath() = byXpath(name, "//div[@accessiblename='Member Properties' and @class='MyDialog']")
    }
}

/**
 * Finds the MemberPropertiesDialog and modifies fixtureStack.
 */
fun ContainerFixture.memberPropertiesDialog(
    fixtureStack: MutableList<Locator>,
    timeout: Duration = Duration.ofSeconds(60),
    function: MemberPropertiesDialog.() -> Unit = {}
) {
    find<MemberPropertiesDialog>(MemberPropertiesDialog.xPath(), timeout).apply {
        fixtureStack.add(MemberPropertiesDialog.xPath())
        function()
        fixtureStack.removeLast()
    }
}