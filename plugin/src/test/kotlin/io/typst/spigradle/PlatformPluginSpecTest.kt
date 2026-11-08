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

import io.typst.spigradle.bungee.BungeePlugin
import io.typst.spigradle.nukkit.NukkitPlugin
import io.typst.spigradle.paper.PaperPlugin
import io.typst.spigradle.spigot.SpigotPlugin
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformPluginSpecTest {
    @Test
    fun `description tasks belong to their own platform`() {
        val project = ProjectBuilder.builder().build()
        val specs = listOf(SpigotPlugin.spec, BungeePlugin.spec, NukkitPlugin.spec, PaperPlugin.spec)

        for (spec in specs) {
            val ctx = spec.createModuleRegistrationContext(project, project.provider { emptyMap() })
            val (detection, generation) = registerDescGenTask(project, ctx)

            assertEquals(spec.platformName, ctx.platformName)
            assertEquals(spec.platformName, detection.get().group)
            assertEquals(spec.platformName, generation.get().group)
        }
    }
}
