package com.xero.intellijplugin.createmodule.param

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class CollectParameters {
    operator fun invoke(project: Project): UserInputParams? {
        val moduleName = Messages.showInputDialog(
            project,
            "In lower case",
            "Please Choose Module Name",
            Messages.getInformationIcon()
        )
        return moduleName?.let { UserInputParams(it) }
    }
}