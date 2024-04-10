package com.xero.intellijplugin.createmodule.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt.createTempDirectory
import com.xero.intellijplugin.createmodule.config.ModuleTypeConfig
import com.xero.intellijplugin.createmodule.param.CollectParameters
import com.xero.intellijplugin.createmodule.util.Messenger
import java.io.File
import java.io.IOException


class CreateModuleAction(
    private val moduleTypeConfig: ModuleTypeConfig,
): AnAction("Create '${moduleTypeConfig.type}' module...") {
    private val collectParameters = CollectParameters()
    private val messenger = Messenger()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val parameters = collectParameters(project) ?: return messenger.displayInfoMessage(project, "Didn't choose a module name")
        val moduleName = parameters.moduleName
        if (checkModuleExists(moduleName)) {
            return messenger.displayInfoMessage(project, "Module '$moduleName' of type '${moduleTypeConfig.type}' already exists!")
        }
        val provisionalDir = copyTemplateToProvisionalDir(moduleName)
        createMainSourceDir(provisionalDir, moduleTypeConfig.rootPackage, moduleName)
        replacePlaceholdersWithValues(provisionalDir, moduleName)
        copyToCurrentProject(provisionalDir, moduleName)
        registerModule(project, moduleName)
        triggerGradleSync(e)
        messenger.displayInfoMessage(project, "Module '${moduleName}' of type '${moduleTypeConfig.type}' has been created!")
    }

    private fun registerModule(project: Project, moduleName: String) {
        for(settingsFileName in arrayOf("settings.gradle", "settings.gradle.kts")) {
            val settingsFile = File(project.basePath, settingsFileName)
            if (!settingsFile.exists()) {
                continue
            }
            val lines = settingsFile.readLines()
            val lastIndexOfModuleOfSameType = lines.indexOfLast { Regex("^include .+:${moduleTypeConfig.type}:.+").matches(it) }
            val newIncludeInsertPosition = if (lastIndexOfModuleOfSameType != -1) {
                lastIndexOfModuleOfSameType
            } else {
                lines.indexOfLast { Regex("^include .+:.+").matches(it) }
            }
            val newLine = if (settingsFileName.endsWith(".kts")) {
                "include (\":${moduleTypeConfig.type}:$moduleName\")"
            } else {
                "include ':${moduleTypeConfig.type}:$moduleName'"
            }
            val linesForOutput = if (newIncludeInsertPosition == -1 || newIncludeInsertPosition == lines.size - 1) {
                lines.plus(newLine)
            } else {
                lines.subList(0, newIncludeInsertPosition + 1).plus(newLine).plus(lines.subList(newIncludeInsertPosition + 1, lines.size))
            }
            settingsFile.writeText(linesForOutput.joinToString("\n"))
        }
    }

    private fun copyToCurrentProject(provisionalDir: File, moduleName: String) {
        provisionalDir.copyRecursively(File(moduleTypeConfig.rootDir, moduleName))
    }

    private fun checkModuleExists(moduleName: String): Boolean {
        return File(moduleTypeConfig.rootDir, moduleName).exists()
    }

    private fun replacePlaceholdersWithValues(provisionalDir: File, moduleName: String) {
        for (gradleFileName in arrayOf("build.gradle", "build.gradle.kts")) {
            val gradleFile = File(provisionalDir, gradleFileName)
            if (!gradleFile.exists()) {
                continue
            }
            val content = gradleFile.readText()
                .replace("MODULE_MOLD_MODULE_NAME", moduleName)
                .replace("MODULE_MOLD_MODULE_PACKAGE", getModulePackage(moduleName))
            gradleFile.writeText(content)
        }
    }

    private fun getModulePackage(moduleName: String): String {
        return "${moduleTypeConfig.rootPackage}.$moduleName"
    }

    private fun createMainSourceDir(provisionalDir: File, rootPackage: String, moduleName: String) {
        val javaFolder = File(provisionalDir, "src/main/java")
        if (javaFolder.exists()) {
            File(javaFolder, rootPackage.replace(".", "/") + "/" + moduleName).mkdirs()
        }
    }

    private fun copyTemplateToProvisionalDir(moduleName: String): File {
        val tempDir = createTempDirectory(/* prefix = */ moduleName, /* suffix = */ null, /* deleteOnExit = */ true)
        val provisionalDir = File(tempDir, moduleName)
        val copySuccess = moduleTypeConfig.templateDir.copyRecursively(provisionalDir)
        if (!copySuccess) {
            throw IOException("Failed to copy from dir '${moduleTypeConfig.templateDir}' to temp dir '$provisionalDir'")
        } else {
            return provisionalDir
        }
    }

    private fun triggerGradleSync(e: AnActionEvent) {
        val am: ActionManager = ActionManager.getInstance()
        val sync: AnAction = am.getAction("Android.SyncProject") ?: return
        sync.actionPerformed(e)
    }
}