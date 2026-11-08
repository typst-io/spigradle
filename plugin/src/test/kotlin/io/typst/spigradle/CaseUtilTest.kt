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

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class CaseUtilTest {
    @Test
    fun case() {
        assertEquals(
            "MyAppProject",
            "myAppProject".asCapitalized()
        )
        assertEquals("myAppProject", "my-app-project".asKebabToCamelCase())
        assertEquals("MyAppProject", "myAppProject".asCapitalized())
        assertEquals("MyAppProject", "my-app-project".asCamelCase(true))
        assertEquals("Main", "main".asCamelCase(true))
    }

    // https://github.com/typst-io/spigradle/issues/118
    @Test
    fun `capitalization does not depend on the default locale`() {
        val defaultLocale = Locale.getDefault()
        try {
            // Turkish title-cases 'i' to dotted 'İ' (U+0130)
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))

            assertEquals("Item", "item".asCapitalized())
            assertEquals("Item", "item".asCamelCase(true))
            assertEquals("ItemShop", "item-shop".asCamelCase(true))
            assertEquals("debugItem", "debug${"item".asCamelCase(true)}")
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }
}
