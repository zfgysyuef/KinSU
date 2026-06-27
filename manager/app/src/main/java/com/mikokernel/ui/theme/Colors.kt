package com.mikokernel.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class KeyColorOption(val color: Int, val name: String)

val keyColorOptions = listOf(
    KeyColorOption(Color(0xFFF44336).toArgb(), "红色"),
    KeyColorOption(Color(0xFFE91E63).toArgb(), "粉红"),
    KeyColorOption(Color(0xFF9C27B0).toArgb(), "紫色"),
    KeyColorOption(Color(0xFF673AB7).toArgb(), "深紫"),
    KeyColorOption(Color(0xFF3F51B5).toArgb(), "靛蓝"),
    KeyColorOption(Color(0xFF2196F3).toArgb(), "蓝色"),
    KeyColorOption(Color(0xFF00BCD4).toArgb(), "青色"),
    KeyColorOption(Color(0xFF009688).toArgb(), "蓝绿"),
    KeyColorOption(Color(0xFF4FAF50).toArgb(), "绿色"),
    KeyColorOption(Color(0xFFFFEB3B).toArgb(), "黄色"),
    KeyColorOption(Color(0xFFFFC107).toArgb(), "琥珀"),
    KeyColorOption(Color(0xFFFF9800).toArgb(), "橙色"),
    KeyColorOption(Color(0xFF795548).toArgb(), "棕色"),
    KeyColorOption(Color(0xFF607D8F).toArgb(), "灰蓝"),
    KeyColorOption(Color(0xFFFF9CA8).toArgb(), "浅粉"),
)
