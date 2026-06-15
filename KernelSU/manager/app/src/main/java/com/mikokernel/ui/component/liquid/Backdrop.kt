package com.mikokernel.ui.component.liquid

import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

abstract class Backdrop {
    open val isCoordinatesDependent: Boolean = false
    open val offsetResidualX: Float = 0f
    open val offsetResidualY: Float = 0f

    abstract fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?,
        downscaleFactor: Int,
    )
}

class BackdropEffectScope(
    var padding: Float = 0f,
    val downscaleFactor: Int = 1,
    val size: IntSize,
    val shape: Shape,
    val layoutDirection: LayoutDirection,
) : Density {
    override val density: Float = 1f
    override val fontScale: Float = 1f
    fun isRuntimeShaderSupported(): Boolean = false

    fun runtimeShaderEffect(
        key: String,
        shaderString: String,
        uniformShaderName: String,
        block: RuntimeShaderEffectScope.() -> Unit,
    ) {}

    fun colorControls(
        brightness: Float = 0f,
        contrast: Float = 1f,
        saturation: Float = 1f,
    ) {}
}

class RuntimeShaderEffectScope {
    fun setFloatUniform(name: String, vararg values: Float) {}
}