package com.xero.intellijplugin.createmodule.config

data class ConfigDto(
    val templateRootDir: String,
    val rootPackage: String,
    val moduleTypes: List<ModuleTypeConfigDto>,
)

data class ModuleTypeConfigDto(
    val type: String,
    val template: String,
)
