/*
 * Copyright (c) 2021 Spigradle contributors.
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

import io.typst.spigradle.findSubclass
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes.ACC_ABSTRACT
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import kotlin.test.assertEquals

class MainDetectionTest {
    val superName = "org/bukkit/api/Plugin"
    val subName = "org/sample/test/MyMain"
    val supersInit = setOf(superName)

    @Test
    fun `when received a wanted sub class`() {
        assertEquals(
            subName to setOf(superName),
            findSubclass(setOf(superName), ACC_PUBLIC, subName, superName)
        )
    }

    @Test
    fun `when received a wanted but a abstract`() {
        val middleClass = "MiddleClass"
        assertEquals(
            null to (supersInit + middleClass),
            findSubclass(supersInit, ACC_PUBLIC or ACC_ABSTRACT, middleClass, superName)
        )
    }

    @Test
    fun `when received a grand sub class`() {
        val middleClass = "MiddleClass"
        val supers = supersInit + middleClass
        assertEquals(
            subName to supers,
            findSubclass(supers, ACC_PUBLIC, subName, middleClass)
        )
    }
}
