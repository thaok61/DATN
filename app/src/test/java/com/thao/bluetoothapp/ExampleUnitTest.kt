package com.thao.bluetoothapp

import com.paramsen.noise.Noise
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val floatArray = floatArrayOf(1f,2f,3f,4f,5f)
        val noise = Noise.real(5)
        val fft = noise.fft(floatArray,FloatArray(7))
        println("FFT: $fft")
    }
}