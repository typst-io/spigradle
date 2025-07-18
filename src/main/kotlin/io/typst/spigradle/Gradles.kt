/*
 * Copyright (c) 2020 Spigradle contributors.
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

import groovy.lang.GroovyObject
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.gradle.ext.RunConfigurationContainer

internal inline fun <T> notNull(any: T?, message: () -> String = { "" }): T {
    return any ?: throw GradleException(message())
}

internal fun TaskContainer.findArtifactJar() =
        listOf("shadowJar", "jar").asSequence()
                .mapNotNull { findByName(it) }
                .filter { it.enabled }
                .filterIsInstance<Jar>()
                .mapNotNull { it.archiveFile.orNull?.asFile }
                .firstOrNull()

internal fun lazyString(provider: () -> Any): Any = object {
    override fun toString(): String {
        return provider().toString()
    }
}

internal fun <T : Task> TaskProvider<T>.applyToConfigure(configure: T.() -> Unit): TaskProvider<T> {
    return apply { configure(configure) }
}

internal inline fun <T> Project.cachingProvider(crossinline provider: () -> T): Provider<T> {
    var cache: T? = null
    return provider {
        if (cache == null) {
            cache = provider()
        }
        cache
    }
}

internal fun IdeaProject.settings(configure: ProjectSettings.() -> Unit = {}) =
        (this as ExtensionAware).extensions.getByType(ProjectSettings::class).apply(configure)

internal fun ProjectSettings.runConfigurations(configure: RunConfigurationContainer.() -> Unit) =
        ((this as ExtensionAware).extensions["runConfigurations"] as RunConfigurationContainer).apply(configure)

internal val Any.groovyExtension get() = (this as GroovyObject).getProperty("ext") as ExtraPropertiesExtension
