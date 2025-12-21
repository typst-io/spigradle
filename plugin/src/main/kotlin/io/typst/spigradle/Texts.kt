/*
 * Copyright (c) 2025 Spigradle contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.typst.spigradle

import org.apache.commons.text.CaseUtils
import java.util.*

private val textDelimiters: Set<Char> = setOf('-', '_')

fun String.asKebabToCamelCase(capitalizeFirstLetter: Boolean = false): String =
    CaseUtils.toCamelCase(this, capitalizeFirstLetter, *textDelimiters.toCharArray())

fun String.asCapitalized(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun String.asCamelCase(capitalizeFirstLetter: Boolean = false): String {
    val index = indexOfFirst {
        it in textDelimiters
    }
    return if (index >= 0) {
        asKebabToCamelCase(capitalizeFirstLetter)
    } else {
        if (capitalizeFirstLetter) {
            asCapitalized()
        } else {
            asCapitalized().replaceFirstChar { it.lowercase() }
        }
    }
}