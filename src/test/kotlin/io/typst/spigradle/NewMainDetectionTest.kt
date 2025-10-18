/*
 * Copyright (c) 2022 Spigradle contributors.
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
import kotlin.test.Test
import kotlin.test.assertEquals

class NewMainDetectionTest {
    @Test
    fun `single detection`() {
        val parentName = "parent"
        val childName = "child"
        val childClass = ClassDefinition(
            publicClass = true,
            abstractClass = false,
            name = childName,
            parentName = parentName
        )
        val parentClass = ClassDefinition(
            publicClass = true,
            abstractClass = true,
            name = parentName,
        )

        val ctxA = DetectionContext()
            .addClassDef(childClass)
            .addClassDef(parentClass)
        assertEquals(
            childClass,
            ctxA.findMainClass(parentName)
        )

        for (isPublic in listOf(true, false)) {
            val ctx = DetectionContext()
                .addClassDef(parentClass)
                .addClassDef(childClass.copy(publicClass = isPublic, abstractClass = true))
            assertEquals(
                null,
                ctx.findMainClass(parentName)
            )
        }
    }

    @Test
    fun `higher detection`() {
        val high = ClassDefinition(
            true,
            false,
            "high"
        )
        val middle = ClassDefinition(
            true,
            true,
            "middle",
            high.name
        )
        val low = ClassDefinition(
            true,
            false,
            "low",
            middle.name
        )
        for (i in 0..1) {
            val ctxA = DetectionContext()
                .addClassDef(high)
                .addClassDef(
                    middle.copy(
                        publicClass = i == 0
                    )
                )
            val mainA = ctxA.findMainClass(high.name)
            assertEquals(
                null,
                mainA
            )
            val ctxB = ctxA.addClassDef(low)
            assertEquals(
                low,
                ctxB.findMainClass(high.name)
            )
        }
    }

    @Test
    fun `unordered higher detection`() {
        val high = "high"
        val middle = "middle"
        val low = "low"
        val highClass = ClassDefinition(
            publicClass = true,
            abstractClass = false,
            name = high
        )
        val middleClass = ClassDefinition(
            publicClass = true,
            abstractClass = true,
            name = middle,
            parentName = high
        )
        val lowClass = ClassDefinition(
            publicClass = true,
            abstractClass = false,
            name = low,
            parentName = middle
        )
        val ctxA = DetectionContext()
            .addClassDef(highClass)
            .addClassDef(lowClass)
        assertEquals(
            null,
            ctxA.findMainClass(high)
        )
        val ctxB = ctxA.addClassDef(middleClass)
        assertEquals(
            lowClass,
            ctxB.findMainClass(high)
        )
    }
}