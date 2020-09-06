package io.bleu.androidx.widget.utils

import android.graphics.Color
import kotlin.math.ceil

private fun computeComponent(startColor: Int, endColor: Int, radio: Float): Int {
    return startColor + ceil((endColor - startColor) * radio.toDouble()).toInt()
}

object Colors {

    fun getColor(startColor: Int, endColor: Int, radio: Float): Int {
        val alphaStart = Color.alpha(startColor)
        val redStart = Color.red(startColor)
        val blueStart = Color.blue(startColor)
        val greenStart = Color.green(startColor)

        val alphaEnd = Color.alpha(endColor)
        val redEnd = Color.red(endColor)
        val blueEnd = Color.blue(endColor)
        val greenEnd = Color.green(endColor)

        val alpha = computeComponent(alphaStart, alphaEnd, radio)
        val red = computeComponent(redStart, redEnd, radio)
        val greed = computeComponent(greenStart, greenEnd, radio)
        val blue = computeComponent(blueStart, blueEnd, radio)

        return Color.argb(alpha, red, greed, blue)
    }
}