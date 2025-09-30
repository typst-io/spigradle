package io.typst.spigradle.util

import kotlin.random.Random

fun Random.nextString(
        size: Int,
        charRange: CharRange = 'a'..'z'
) = buildString {
    repeat(size) { charRange.random() }
}