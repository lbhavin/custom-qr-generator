package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.github.alexzhirkevich.customqrgenerator.style.*
import kotlin.math.roundToInt

interface QrOptionsBuilderScope {
    val size : Int
    val padding :Float
    var colors : QrColors
    var logo: QrLogo?
    var backgroundImage : QrBackgroundImage?
    var elementsShapes : QrElementsShapes
    var codeShape : QrShape
    var errorCorrectionLevel : QrErrorCorrectionLevel
}

data class QrOptions(
    @IntRange(from = 0) val size : Int,
    @FloatRange(from = .0, to = .5) val padding : Float,
    val colors : QrColors,
    val logo: QrLogo?,
    val background: QrBackgroundImage?,
    val shapes: QrElementsShapes,
    val codeShape : QrShape,
    val errorCorrectionLevel: QrErrorCorrectionLevel
){
    class Builder(@IntRange(from = 0) override val size : Int) : QrOptionsBuilderScope {

        override var padding = .125f
        override var colors = QrColors()
        override var logo: QrLogo? = null
        override var backgroundImage: QrBackgroundImage? = null
        override var elementsShapes = QrElementsShapes()
        override var codeShape : QrShape = QrShape.Default
        override var errorCorrectionLevel : QrErrorCorrectionLevel = QrErrorCorrectionLevel.Auto

        fun build() : QrOptions = QrOptions(
            size, padding,colors, logo, backgroundImage,elementsShapes, codeShape, errorCorrectionLevel
        )

        /**
         * Padding of the QR code relative to [size].
         * */
        fun setPadding(@FloatRange(from = 0.0, to = .5) padding: Float) = apply {
            this.padding = padding
        }

        fun setColors(colors: QrColors) = apply{
            this.colors = colors
        }

        fun setLogo(logo : QrLogo?) = apply {
            this.logo = logo

        }

        fun setBackground(background: QrBackgroundImage?)  = apply{
            this.backgroundImage = background
        }

        fun setCodeShape(shape: QrShape) : Builder = apply{
            this.codeShape =shape
        }

        fun setElementsShapes(shapes: QrElementsShapes) = apply {
            this.elementsShapes = shapes
        }

        fun setErrorCorrectionLevel(level: QrErrorCorrectionLevel) = apply{
            errorCorrectionLevel = level
        }
    }
}

/**
 * Build [QrOptions] with Kotlin DSL
 * */
fun createQrOptions(size: Int, padding: Float = .125f, build : QrOptionsBuilderScope.() -> Unit) : QrOptions {
    return QrOptions.Builder(size)
        .apply { setPadding(padding) }
        .apply(build)
        .build()
}

/**
 * Create a custom QR element shape by drawing on Canvas.
 * Should be used in [createQrOptions] builder.
 *
 * @return [T] shape modifier linked to built [QrOptions].
 * Should not be used for [QrOptions] with changed [QrOptions.size] or
 * [QrOptions.padding]
 * */
inline fun <reified T : QrShapeModifier> QrOptionsBuilderScope.drawElementShape(
    crossinline draw : (canvas : Canvas, drawPaint : Paint, erasePaint : Paint) -> Unit
): T = object : QrCanvasShapeModifier {

    override fun draw(canvas: Canvas, drawPaint: Paint, erasePaint: Paint) =
        draw(canvas, drawPaint, erasePaint)

}.let {
    when{
        T::class == QrPixelShape::class -> it
            .toShapeModifier((size * (1-padding)/ 21).roundToInt())
            .asPixelShape()
        T::class == QrBallShape::class -> it
            .toShapeModifier((size * (1-padding)/ 7).roundToInt())
            .asBallShape()
        T::class == QrFrameShape::class -> it
            .toShapeModifier((size * (1-padding)/ 3).roundToInt())
            .asFrameShape()
        T::class == QrLogoShape::class -> it
            .toShapeModifier((size * (1 - padding)/3).roundToInt())
            .asLogoShape()
        T::class == QrBackgroundShape::class -> it
            .toShapeModifier((size * (1 - padding)/3).roundToInt())
            .asBackgroundShape()
        else -> throw IllegalStateException(
            "Only QrElementsShapes arguments can be created via drawShape function"
        )
    } as T
}