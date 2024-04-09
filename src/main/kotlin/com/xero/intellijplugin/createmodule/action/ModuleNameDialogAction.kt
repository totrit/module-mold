package com.xero.intellijplugin.createmodule.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.xero.intellijplugin.createmodule.config.ModuleTypeConfig
import com.xero.intellijplugin.createmodule.param.CollectParameters
import com.xero.intellijplugin.createmodule.param.UserInputParams
import com.xero.intellijplugin.createmodule.util.Messenger
import java.io.File

class ModuleNameDialogAction(
    private val moduleTypeConfig: ModuleTypeConfig,
): AnAction("Create '${moduleTypeConfig.type}' module...") {
    private val collectParameters = CollectParameters()
    private val messenger = Messenger()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val parameters = collectParameters(project) ?: return messenger.displayInfoMessage(project, "Didn't choose a module name")
        copyFromTemplate(parameters)
    }

    private fun copyFromTemplate(userInputParams: UserInputParams) {
        moduleTypeConfig.templateDir.copyRecursively(File(moduleTypeConfig.rootDir, userInputParams.packageName))
    }
}