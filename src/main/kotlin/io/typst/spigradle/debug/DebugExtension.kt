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

package io.typst.spigradle.debug

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Configuration extension for the Spigot debug system.
 *
 * **Important:** `debugSpigot` is a configuration extension block, NOT a Gradle task.
 * The actual task that runs the debug server is named `debug${ProjectName}` (e.g., `debugMyPlugin`).
 *
 * This extension allows you to configure the local Paper/Spigot server used for debugging
 * your plugin. It controls server download, EULA acceptance, JVM settings, and debug options.
 *
 * **Example usage:**
 * ```groovy
 * debugSpigot {
 *     version.set("1.21.8")
 *     eula.set(true)
 *     jvmDebugPort.set(5005)
 *     jvmArgs.add("-Xmx2G")
 *     programArgs.add("--nogui")
 * }
 * ```
 *
 * **Running the debug server:**
 * ```bash
 * ./gradlew debugMyPlugin  // NOT debugSpigot!
 * ```
 *
 * **Note:** To add custom arguments while keeping defaults:
 * ```groovy
 * jvmArgs.add("-myCustomArg")  // Adds to existing default args
 * ```
 *
 * @see io.typst.spigradle.debug.DebugTask
 */
open class DebugExtension(project: Project) {
    /**
     * The Minecraft/Paper version to download and run (e.g., "1.21.8", "1.20.6").
     *
     * This version is used to download the Paper server JAR from the PaperMC API.
     * If not set, the debug system may fail to download the server.
     *
     * **Example:**
     * ```groovy
     * version.set("1.21.8")
     * ```
     */
    val version: Property<String> = project.objects.property(String::class.java)

    /**
     * Whether to automatically accept the Minecraft EULA.
     *
     * When set to `true`, creates/updates `eula.txt` with `eula=true` in the debug server directory.
     * The debug task will fail if this is `false` and you haven't manually accepted the EULA.
     *
     * **Default:** `false`
     *
     * **Example:**
     * ```groovy
     * eula.set(true)
     * ```
     *
     * See: [Minecraft EULA](https://www.minecraft.net/en-us/eula)
     */
    val eula: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    /**
     * The port for remote JVM debugging.
     *
     * This port is used by the `Debug${ProjectName}` IDEA run configuration to attach
     * to the running server for remote debugging.
     *
     * **Default:** `5005`
     *
     * **Example:**
     * ```groovy
     * jvmDebugPort.set(5006)  // Use different port
     * ```
     *
     * **JVM debug arguments:** The debug system automatically adds:
     * ```
     * -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${jvmDebugPort}
     * ```
     */
    val jvmDebugPort: Property<Int> = project.objects.property(Int::class.java).convention(5005)

    /**
     * Whether to download plugins listed in `softDepends` in addition to `depends`.
     *
     * - When `true`: Downloads both `depends` and `softDepends` plugins
     * - When `false`: Only downloads `depends` plugins
     *
     * **Default:** `false`
     *
     * **Example:**
     * ```groovy
     * downloadSoftDepends.set(true)
     * ```
     *
     * @see io.typst.spigradle.spigot.PluginDependencyPrepareTask
     */
    val downloadSoftDepends: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    /**
     * Additional JVM arguments to pass to the server process.
     *
     * These are added to the default JVM arguments which include debug agent configuration
     * and memory settings.
     *
     * **Common examples:**
     * - `-Xmx2G` - Maximum heap size
     * - `-Xms1G` - Initial heap size
     * - `-XX:+UseG1GC` - Use G1 garbage collector
     *
     * **Example:**
     * ```groovy
     * jvmArgs.addAll(["-Xmx2G", "-Xms1G"])
     * ```
     */
    val jvmArgs: ListProperty<String> = project.objects.listProperty(String::class.java)

    /**
     * Program arguments to pass to the Minecraft server.
     *
     * These arguments are passed directly to the server JAR when it starts.
     *
     * **Common examples:**
     * - `--nogui` - Run without GUI (recommended for headless servers)
     * - `--world world_name` - Specify world name
     * - `--port 25566` - Custom server port
     *
     * **Example:**
     * ```groovy
     * programArgs.add("--nogui")
     * ```
     */
    val programArgs: ListProperty<String> = project.objects.listProperty(String::class.java)
}
