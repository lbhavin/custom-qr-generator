package com.github.alexzhirkevich.customqrgenerator.style

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random


/**
 * Shape of the QR-code pattern.
 * */
interface QrShape {

    val shapeSizeIncrease : Float

    /**
     * Transform actual [matrix] or create new with BIGGER size.
     * Matrix reducing causes [IllegalStateException].
     * */
    fun apply(matrix: QrCodeMatrix) : QrCodeMatrix

    /**
     * Decide if pixel fits inside a QR code shape.
     * [modifiedByteMatrix] is already changed by [apply].
     * */
    fun pixelInShape(i : Int, j : Int, modifiedByteMatrix: QrCodeMatrix) : Boolean

    object Default : QrShape {
        override val shapeSizeIncrease: Float = 1f

        override fun apply(matrix: QrCodeMatrix) = matrix

        override fun pixelInShape(i: Int, j: Int, modifiedByteMatrix: QrCodeMatrix)  = true
    }

    data class Circle(
        @FloatRange(from = 1.0, to = 2.0)
        val padding : Float = 1.1f,
        private val random : Random = Random
    ) : QrShape {

        override fun pixelInShape(i: Int, j: Int, modifiedByteMatrix: QrCodeMatrix): Boolean =
            with(modifiedByteMatrix) {
                val center = size/2f
                return sqrt((center - i).pow(2) + (center-j).pow(2)) <= center
            }

        override val shapeSizeIncrease: Float =
             1 + (padding * sqrt(2.0) - 1).toFloat()

        override fun apply(matrix: QrCodeMatrix): QrCodeMatrix = with(matrix){

            val padding = padding.coerceIn(1f,2f)
            val added = (((size * padding * sqrt(2.0)) - size)/2).roundToInt()

            val newSize = size + 2*added
            val newMatrix = QrCodeMatrix(newSize)

            val center = newSize / 2f

            for (i in 0 until newSize) {
                for (j in 0 until newSize) {
                    if (random.nextBoolean() &&
                        (i <= added-1 ||
                                j <= added-1 ||
                                i >= added + size ||
                                j >= added + size ) &&
                        sqrt((center-i) *(center-i)+(center-j)*(center-j)) <= center
                    ){
                        newMatrix[i, j] = QrCodeMatrix.PixelType.DarkPixel
                    }
                }
            }

            for(i in 0 until size){
                for(j in 0 until size){
                    newMatrix[added+i,added+j] = this[i,j]
                }
            }
            return newMatrix
        }
    }
}