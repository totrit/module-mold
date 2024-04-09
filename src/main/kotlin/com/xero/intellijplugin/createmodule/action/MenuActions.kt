package com.xero.intellijplugin.createmodule.action

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.xero.intellijplugin.createmodule.config.ConfigParser

class MenuActions: ActionGroup() {
    private val configParser = ConfigParser()

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val project = e?.project ?: return emptyArray()
        val config = configParser.parse(project)
        return config.moduleTypes.map {
            ModuleNameDialogAction(
                it
            )
        }.toTypedArray()
    }
}