/*
 * Copyright (c) 2026 Spigradle contributors.
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

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// https://github.com/typst-io/spigradle/issues/128
class SubclassDetectionTest {
    @TempDir
    lateinit var dir: File

    private val superclass = "org/bukkit/plugin/java/JavaPlugin"
    private val classesDir get() = dir.resolve("classes")
    private val outputFile get() = dir.resolve("main.txt")

    private fun writeClass(name: String, parentName: String) {
        val writer = ClassWriter(0)
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, name, null, parentName, null)
        writer.visitEnd()
        classesDir.resolve("$name.class").apply { parentFile.mkdirs() }.writeBytes(writer.toByteArray())
    }

    private fun detect() {
        val project = ProjectBuilder.builder().withProjectDir(dir).build()
        project.tasks.register("detect", SubclassDetection::class.java).get().apply {
            classDirectories.from(classesDir)
            outputFileBySuperclass.put(superclass, project.layout.projectDirectory.file(outputFile.name))
        }.inspect()
    }

    @Test
    fun `writes the only candidate`() {
        writeClass("com/example/Main", superclass)

        detect()

        assertEquals("com.example.Main", outputFile.readText())
    }

    @Test
    fun `deletes the previous result when there are multiple candidates`() {
        outputFile.writeText("com.example.Stale")
        writeClass("com/example/Main", superclass)
        writeClass("com/example/Other", superclass)

        detect()

        assertFalse(outputFile.exists())
    }

    @Test
    fun `deletes the previous result when there is no candidate`() {
        outputFile.writeText("com.example.Stale")
        writeClass("com/example/Util", "java/lang/Object")

        detect()

        assertFalse(outputFile.exists())
    }
}

