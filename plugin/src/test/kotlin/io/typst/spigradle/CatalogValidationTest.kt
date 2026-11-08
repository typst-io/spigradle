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

import io.typst.spigradle.catalog.SpigradleCatalogPublishPlugin
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class CatalogValidationTest {
    @TempDir
    lateinit var dir: File

    private fun runner(
        libraries: String = "",
        versions: String = "new Version('1.0', 'example')",
        plugins: String = "",
    ): GradleRunner {
        val catalogClasspath = File(SpigradleCatalogPublishPlugin::class.java.protectionDomain.codeSource.location.toURI())
        dir.resolve("catalog-classpath.txt").writeText(catalogClasspath.absolutePath)
        dir.resolve("settings.gradle").writeText("rootProject.name = 'catalog-test'")
        dir.resolve("build.gradle").writeText(
            """
            import io.typst.spigradle.catalog.Dependency
            import io.typst.spigradle.catalog.PluginDependency
            import io.typst.spigradle.catalog.Version

            buildscript {
                dependencies {
                    classpath files(file('catalog-classpath.txt').text)
                }
            }
            apply plugin: io.typst.spigradle.catalog.SpigradleCatalogPublishPlugin

            spigradleCatalog {
                versions.set([$versions])
                plugins.set([$plugins])
                libraries.set([$libraries])
            }
            """.trimIndent()
        )
        return GradleRunner.create().withProjectDir(dir).withArguments("generateCatalogAsToml", "--stacktrace")
    }

    @Test
    fun `rejects duplicate version labels and identifies both versions`() {
        val result = runner(versions = "new Version('1.0', 'duplicate'), new Version('2.0', 'duplicate')")
            .buildAndFail()

        assertTrue(result.output.contains("Duplicated the version label 'duplicate' between '1.0' and '2.0'"), result.output)
    }

    @Test
    fun `rejects duplicate plugin labels and identifies both plugin ids`() {
        val result = runner(
            plugins = """
            new PluginDependency('example.first', new Version('1.0', 'example'), 'duplicate'),
            new PluginDependency('example.second', new Version('1.0', 'example'), 'duplicate')
            """.trimIndent()
        ).buildAndFail()

        assertTrue(result.output.contains("Duplicated the plugin label 'duplicate'"), result.output)
        assertTrue(result.output.contains("example.first"), result.output)
        assertTrue(result.output.contains("example.second"), result.output)
    }

    @Test
    fun `rejects duplicate library labels and identifies both coordinates`() {
        val result = runner(
            """
            new Dependency('example', 'first', new Version('1.0', 'example'), 'duplicate', false, []),
            new Dependency('example', 'second', new Version('1.0', 'example'), 'duplicate', false, [])
            """.trimIndent()
        ).buildAndFail()

        assertTrue(result.output.contains("Duplicated the library label 'duplicate'"), result.output)
        assertTrue(result.output.contains("example:first"), result.output)
        assertTrue(result.output.contains("example:second"), result.output)
    }

    @Test
    fun `accepts distinct library labels`() {
        runner(
            """
            new Dependency('example', 'first', new Version('1.0', 'example'), 'first', false, []),
            new Dependency('example', 'second', new Version('1.0', 'example'), 'second', false, [])
            """.trimIndent()
        ).build()

        val catalog = dir.resolve("build/version-catalog/libs.versions.toml").readText()
        assertTrue(catalog.contains("first ="), catalog)
        assertTrue(catalog.contains("second ="), catalog)
    }
}
