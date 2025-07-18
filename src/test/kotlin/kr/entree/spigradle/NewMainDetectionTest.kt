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

package kr.entree.spigradle

import io.typst.spigradle.detection.ClassDefinition
import io.typst.spigradle.detection.findSubClass
import org.junit.jupiter.api.Test
import kotlin.test.Ignore
import kotlin.test.assertEquals

@Ignore
class NewMainDetectionTest {
    @Test
    fun unit() {
        assertEquals(
            null to emptySet(),
            findSubClass(
                ClassDefinition.empty,
                emptySet()
            )
        )
    }

    @Test
    fun `single detection`() {
        val parentName = "parent"
        val childName = "child"
        val base = ClassDefinition(
            publicClass = true,
            abstractClass = false,
            name = childName,
            parentName = parentName
        )
        assertEquals(
            childName to setOf(parentName),
            findSubClass(
                base,
                setOf(parentName)
            )
        )
        assertEquals(
            null to setOf(parentName),
            findSubClass(
                base.copy(publicClass = false),
                setOf(parentName)
            )
        )
        for (isPublic in listOf(true, false)) {
            assertEquals(
                null to setOf(parentName, childName),
                findSubClass(
                    base.copy(publicClass = isPublic, abstractClass = true),
                    setOf(parentName)
                )
            )
        }
    }

    @Test
    fun `higher detection`() {
        val high = "high"
        val middle = "middle"
        val low = "low"

        for (i in 0..1) {
            val (subA, supersA) = findSubClass(
                ClassDefinition(
                    publicClass = i == 0,
                    abstractClass = true,
                    name = middle,
                    parentName = high
                ),
                setOf(high)
            )
            assertEquals(
                null to setOf(middle, high),
                subA to supersA
            )
            val (subB, supersB) = findSubClass(
                ClassDefinition(
                    publicClass = true,
                    abstractClass = false,
                    name = low,
                    parentName = middle
                ),
                supersA
            )
            assertEquals(
                low to setOf(middle, high),
                subB to supersB
            )
        }
    }

    @Test
    fun `unordered higher detection`() {
        val high = "high"
        val middle = "middle"
        val low = "low"
        val (subA, supersA) = findSubClass(
            ClassDefinition(
                publicClass = true,
                abstractClass = false,
                name = low,
                parentName = middle
            ),
            setOf(high)
        )
        assertEquals(
            null,
            subA
        )
        val (subB, supersB) = findSubClass(
            ClassDefinition(
                publicClass = true,
                abstractClass = true,
                name = middle,
                parentName = high
            ),
            supersA
        )
        assertEquals(
            low to setOf(middle, high),
            subB to supersB
        )
    }
}