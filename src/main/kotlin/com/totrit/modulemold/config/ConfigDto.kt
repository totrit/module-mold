package com.totrit.modulemold.config

data class ConfigDto(
    val templateRootDir: String,
    val rootPackage: String,
    val moduleTypes: List<ModuleTypeConfigDto>,
)

data class ModuleTypeConfigDto(
    val type: String,
    val template: String,
    val rootDir: String?,
)
