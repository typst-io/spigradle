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

import io.typst.spigradle.detection.ClassDefinition
import io.typst.spigradle.detection.DetectionContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MainDetectionTest {
    val superName = "org/bukkit/api/Plugin"
    val subName = "org/sample/test/MyMain"
    internal val superClass: ClassDefinition = ClassDefinition(
        publicClass = true,
        abstractClass = true,
        name = superName
    )
    internal val subClass: ClassDefinition = ClassDefinition(
        publicClass = true,
        abstractClass = false,
        name = subName,
        parentName = superName
    )
    internal val middleClass = ClassDefinition(
        publicClass = true,
        abstractClass = true,
        name = "MiddleClass",
        parentName = superName
    )

    @Test
    fun `when received a wanted sub class`() {
        val ctx = DetectionContext()
            .addClassDef(subClass)
            .addClassDef(superClass)
        assertEquals(
            subClass,
            ctx.findMainClass(superName)
        )
    }

    @Test
    fun `when received a wanted but a abstract`() {
        assertEquals(
            null,
            DetectionContext()
                .addClassDef(superClass)
                .addClassDef(middleClass)
                .findMainClass(middleClass.name)
        )
    }

    @Test
    fun `when received a grand sub class`() {
        val subClass = subClass.copy(parentName = middleClass.name)
        val ctx = DetectionContext()
            .addClassDef(superClass)
            .addClassDef(middleClass)
            .addClassDef(subClass)
        assertEquals(
            subClass,
            ctx.findMainClass(superName)
        )
    }
}
