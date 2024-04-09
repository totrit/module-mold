package com.xero.intellijplugin.createmodule.config

import java.io.File

data class Config(
    val packagePrefix: String,
    val moduleTypes: List<ModuleTypeConfig>
)

data class ModuleTypeConfig(
    val type: String,
    val templateDir: File,
    val rootDir: File,
)
