package com.mikokernel.data.model

data class KpmModuleInfo(
    val id: String,
    val name: String,
    val version: String,
    val license: String,
    val author: String,
    val description: String,
    val args: String,
    val loaded: Boolean,
    val persistent: Boolean,
)

data class KpmFileMetadata(
    val id: String,
    val version: String,
    val license: String,
    val author: String,
    val description: String,
    val args: String,
)
