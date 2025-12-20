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
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Provider

internal val Any.groovyExtension get() = (this as GroovyObject).getProperty("ext") as ExtraPropertiesExtension

internal val Project.hasJavaPlugin: Boolean get() = pluginManager.hasPlugin("java")

internal val Project.hasJavaBasePlugin: Boolean get() = pluginManager.hasPlugin("java-base")

internal fun <A : Any> List<Provider<out A>>.sequence(project: Project): Provider<List<A>> {
    return project.provider {
        map {
            it.get()
        }
    }
}

internal fun <A, B : Any> Map<out A, Provider<out B>>.sequenceMap(project: Project): Provider<Map<A, B>> {
    return toList().map { (a, fb) ->
        fb.map { a to it }
    }.sequence(project).map { it.toMap() }
}