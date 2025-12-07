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
import org.gradle.api.file.Directory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

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
 *     jvmArgs.append("-Xmx2G")       // use append() to preserve defaults
 *     programArgs.append("--world myworld")
 * }
 * ```
 *
 * **Running the debug server:**
 * ```bash
 * ./gradlew debugMyPlugin  // NOT debugSpigot!
 * ```
 *
 * **Note:** To add custom arguments while keeping defaults (Gradle 8.7+):
 * ```groovy
 * jvmArgs.append("-myCustomArg")  // Preserves existing defaults
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
     * JVM arguments to pass to the server process.
     *
     * **Warning:** Each platform plugin sets different default values for this property.
     * Using `add()`, `addAll()`, `set()`, or `empty()` will **discard** the platform-specific defaults.
     * To preserve defaults while adding your own arguments, use `append()` or `appendAll()` (Gradle 8.7+).
     *
     * **Common examples:**
     * - `-Xmx2G` - Maximum heap size
     * - `-Xms1G` - Initial heap size
     * - `-XX:+UseG1GC` - Use G1 garbage collector
     *
     * **Example:**
     * ```groovy
     * // Recommended (Gradle 8.7+): preserves platform defaults
     * jvmArgs.append("-Xmx2G")
     * jvmArgs.appendAll(["-Xms1G", "-XX:+UseG1GC"])
     *
     * // Caution: these discard platform defaults!
     * jvmArgs.add("-Xmx2G")      // convention lost
     * jvmArgs.set(["-Xmx2G"])    // convention lost
     * ```
     *
     * @see io.typst.spigradle.spigot.SpigotPlugin
     * @see <a href="https://docs.gradle.org/8.7/release-notes.html">Gradle 8.7 Release Notes</a>
     */
    val jvmArgs: ListProperty<String> = project.objects.listProperty(String::class.java)

    /**
     * Program arguments to pass to the Minecraft server.
     *
     * These arguments are passed directly to the server JAR when it starts.
     *
     * **Warning:** Each platform plugin sets different default values for this property.
     * Using `add()`, `addAll()`, `set()`, or `empty()` will **discard** the platform-specific defaults.
     * To preserve defaults while adding your own arguments, use `append()` or `appendAll()` (Gradle 8.7+).
     *
     * **Common examples:**
     * - `nogui` - Run without GUI (recommended for headless servers)
     * - `--world world_name` - Specify world name
     * - `--port 25566` - Custom server port
     *
     * **Example:**
     * ```groovy
     * // Recommended (Gradle 8.7+): preserves platform defaults
     * programArgs.append("--world myworld")
     *
     * // Caution: these discard platform defaults!
     * programArgs.add("--world myworld")  // convention lost
     * programArgs.set(["nogui"])          // convention lost
     * ```
     *
     * @see <a href="https://docs.gradle.org/8.7/release-notes.html">Gradle 8.7 Release Notes</a>
     */
    val programArgs: ListProperty<String> = project.objects.listProperty(String::class.java)

    /**
     * The Java language version to use for running the debug server.
     *
     * This property is a convenience helper for resolving [javaHome].
     * If [javaHome] is set directly, this property is ignored.
     *
     * **Default:** The Java toolchain language version configured in the project's
     * `java.toolchain.languageVersion`.
     *
     * **Example:**
     * ```groovy
     * javaVersion.set(JavaLanguageVersion.of(21))
     * ```
     *
     * @see javaHome
     */
    val javaVersion: Property<JavaLanguageVersion> = project.objects.property(JavaLanguageVersion::class.java)
        .convention(project.provider {
            val javaExt = project.extensions.getByType(JavaPluginExtension::class.java)
            val toolchainSpec = javaExt.toolchain
            val javaToolchains = project.extensions.getByType(JavaToolchainService::class.java)
            val launcherProvider = javaToolchains.launcherFor(toolchainSpec)
            launcherProvider.get().metadata.languageVersion
        })

    /**
     * The Java installation home directory to use for running the debug server.
     *
     * This value corresponds to `JAVA_HOME` and is used to locate the `java` executable
     * at `{javaHome}/bin/java`.
     *
     * **Default:** Resolved from [javaVersion] using Gradle's Java Toolchain Service.
     * If you set this property directly, the [javaVersion] value will be ignored.
     *
     * **Example:**
     * ```groovy
     * // Use a specific Java installation
     * javaHome.set(layout.projectDirectory.dir("/path/to/jdk"))
     *
     * // Or let it be resolved from javaVersion (default behavior)
     * javaVersion.set(JavaLanguageVersion.of(21))
     * ```
     *
     * @see javaVersion
     */
    val javaHome: Property<Directory> = project.objects.directoryProperty()
        .convention(javaVersion.flatMap { javaVersion ->
            val toolchains = project.extensions.getByType(JavaToolchainService::class.java)
            toolchains.launcherFor {
                languageVersion.set(javaVersion)
            }.map {
                it.metadata.installationPath
            }
        })
}
