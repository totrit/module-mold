package com.xero.intellijplugin.createmodule.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intellij.openapi.project.Project
import java.io.File

class ConfigParser {
    fun parse(project: Project): Config {
        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(KotlinModule.Builder().build())
        return map(project, mapper.readValue(File(project.basePath, "module-mold.yaml"), ConfigDto::class.java))
    }

    private fun map(project: Project, dto: ConfigDto): Config {
        return Config(
            packagePrefix = dto.packagePrefix,
            moduleTypes = dto.moduleTypes.map {
                ModuleTypeConfig(
                    type = it.type,
                    templateDir = File(File(project.basePath, dto.templateRootDir), it.template),
                    rootDir = File(project.basePath, it.type),
                )
            }
        )
    }
}