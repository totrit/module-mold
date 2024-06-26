package com.totrit.modulemold.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt.createTempDirectory
import com.totrit.modulemold.config.ModuleTypeConfig
import com.totrit.modulemold.param.CollectParameters
import com.totrit.modulemold.util.Messenger
import java.io.File
import java.io.IOException


class CreateModuleAction(
    private val moduleTypeConfig: ModuleTypeConfig,
): AnAction("Create ${getArticleForWord(moduleTypeConfig.type)} '${moduleTypeConfig.type}' module...") {
    private val collectParameters = CollectParameters()
    private val messenger = Messenger()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val parameters = collectParameters(project) ?: return messenger.displayInfoMessage(
            "Module creation is abandoned because no module name is chosen.",
            isError = true
            )
        val moduleName = parameters.moduleName
        if (checkModuleExists(moduleName)) {
            return messenger.displayInfoMessage(
                "Module '$moduleName' of type '${moduleTypeConfig.type}' already exists!",
                isError = true
            )
        }
        val provisionalDir = copyTemplateToProvisionalDir(moduleName)
        replacePlaceholderDir(provisionalDir, moduleName)
        replacePlaceholdersWithValues(provisionalDir, moduleName)
        copyToCurrentProject(provisionalDir, moduleName)
        registerModule(project, moduleName)
        triggerGradleSync(e)
        messenger.displayInfoMessage(
            "Module '${moduleName}' of type '${moduleTypeConfig.type}' has been created!",
            isError = false
        )
    }

    private fun registerModule(project: Project, moduleName: String) {
        for(settingsFileName in arrayOf("settings.gradle", "settings.gradle.kts")) {
            val settingsFile = File(project.basePath, settingsFileName)
            if (!settingsFile.exists()) {
                continue
            }
            val lines = settingsFile.readLines()
            val reg = Regex("^include [^:]+:(${moduleTypeConfig.type}:[^:]+).+")
            var bestInsertPositionOfSameModuleType = -1
            var hasSeenSameTypeEntry = false
            for((index, s) in lines.withIndex()) {
                val matchResult = reg.find(s)
                if (matchResult != null) {
                    hasSeenSameTypeEntry = true
                    bestInsertPositionOfSameModuleType = index
                    if ("${moduleTypeConfig.type}:$moduleName" < matchResult.groupValues[1]) {
                        break
                    }
                } else if (hasSeenSameTypeEntry) {
                    bestInsertPositionOfSameModuleType = index
                    break
                }
            }
            val newIncludeInsertPosition = if (bestInsertPositionOfSameModuleType != -1) {
                bestInsertPositionOfSameModuleType
            } else {
                lines.indexOfLast { Regex("^include .+:.+").matches(it) }
            }
            val moduleId = if (moduleTypeConfig.rootDir.path == project.basePath) {
                ":$moduleName"
            } else {
                ":${moduleTypeConfig.rootDir.name}:$moduleName"
            }
            val newLine = if (settingsFileName.endsWith(".kts")) {
                "include (\"$moduleId\")"
            } else {
                "include '$moduleId'"
            }
            val linesForOutput = if (newIncludeInsertPosition == -1 || newIncludeInsertPosition == lines.size - 1) {
                lines.plus(newLine)
            } else {
                lines.subList(0, newIncludeInsertPosition).plus(newLine).plus(lines.subList(newIncludeInsertPosition, lines.size))
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

    private fun replacePlaceholderDir(provisionalDir: File, moduleName: String) {
        val srcFolder = File(provisionalDir, "src")
        srcFolder.walk().filter { it.isDirectory && it.name == "MODULE_MOLD_MODULE_PACKAGE" }.forEach { placeholderDir ->
            val packageDir = File(placeholderDir.parentFile, getModulePackage(moduleName).replace(".", "/"))
            packageDir.mkdirs()
            placeholderDir.listFiles()?.filterNot { it.name == ".gitkeep" }?.forEach { it.copyRecursively(File(packageDir, it.name)) }
            placeholderDir.deleteRecursively()
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

    private companion object {
        fun getArticleForWord(word: String): String {
            if (arrayOf("a", "e", "i", "o", "u").any { word.startsWith(it, ignoreCase = true) }) {
                return "an"
            } else {
                return "a"
            }
        }
    }
}