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
 * The debug extension for each plugin
 *
 * There is default value each plugin, if you want to add a custom arg with the default then:
 * ```
 * jvmArgs.add("-myCustomArg")
 * ```
 */
open class DebugExtension(project: Project) {
    val version: Property<String> = project.objects.property(String::class.java)
    val eula: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)
    val jvmDebugPort: Property<Int> = project.objects.property(Int::class.java).convention(5005)
    val downloadSoftDepends: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)
    val jvmArgs: ListProperty<String> = project.objects.listProperty(String::class.java)
    val programArgs: ListProperty<String> = project.objects.listProperty(String::class.java)
}
