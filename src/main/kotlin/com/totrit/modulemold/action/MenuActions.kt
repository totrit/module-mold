package com.totrit.modulemold.action

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.totrit.modulemold.config.ConfigParser
import java.io.File

class MenuActions: ActionGroup() {
    private val configParser = ConfigParser()

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val project = e?.project ?: return arrayOf(NoProjectOpenAction())
        if (!File(project.basePath, "module-mold.yaml").exists()) {
            return  arrayOf(NoConfigFoundAction())
        }
        val config = configParser.parse(project)
        return config.moduleTypes.map {
            CreateModuleAction(
                it
            )
        }.toTypedArray()
    }
}