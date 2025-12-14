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

package io.typst.spigradle.catalog

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.catalog.CatalogPluginExtension

// used from `*-catalog` modules
class SpigradleCatalogPublishPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val spigradleCatalog = project.extensions.create("spigradleCatalog", SpigradleCatalogExtension::class.java)
        project.pluginManager.apply("version-catalog")
        project.pluginManager.apply("maven-publish")
        val ext = project.extensions.getByName("catalog") as CatalogPluginExtension

        project.afterEvaluate {
            ext.versionCatalog {
                for (dep in spigradleCatalog.libraries.get()) {
                    version(dep.versionRef, dep.version)
                    library(dep.alias, dep.group, dep.artifact).versionRef(dep.versionRef)
                }
                for (dep in spigradleCatalog.plugins.get()) {
                    version(dep.versionRef, dep.version)
                    plugin(dep.alias, "${dep.group}.${dep.artifact}").versionRef(dep.versionRef)
                }
            }
        }
    }
}