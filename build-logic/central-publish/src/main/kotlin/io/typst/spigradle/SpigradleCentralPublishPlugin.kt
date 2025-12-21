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

package io.typst.spigradle

import org.apache.commons.text.CaseUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.getByName
import org.gradle.plugins.signing.SigningExtension
import java.net.URI

class SpigradleCentralPublishPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("maven-publish")
        val hasSign = project.hasProperty("signing.keyId")
        if (hasSign) {
            project.pluginManager.apply("signing")
        }
        val publishing = project.extensions.getByName("publishing") as PublishingExtension
        val moduleName = CaseUtils.toCamelCase(project.name, false, '-', '_')
        publishing.publications {
            val catalog = project.components.findByName("versionCatalog")
            val javaPlatform = project.components.findByName("javaPlatform")
            create(moduleName, MavenPublication::class.java) {
                if (catalog != null) {
                    from(catalog)
                } else if (javaPlatform != null) {
                    from(javaPlatform)
                }
                pom {
                    name.set("${project.group}:${project.name}")
                    description.set(project.provider { project.description.toString() })
                    url.set("https://github.com/typst-io/spigradle")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("entrypointkr")
                            name.set("Junhyung Im")
                            email.set("entrypointkr@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/typst-io/spigradle.git")
                        developerConnection.set("scm:git:ssh://github.com:typst-io/spigradle.git")
                        url.set("https://github.com/typst-io/spigradle/tree/master")
                    }
                }
            }
        }
        publishing.repositories {
            maven {
                name = "sonatypeReleases"
                url = URI.create("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                credentials {
                    username = project.findProperty("ossrhUsername")?.toString()
                    password = project.findProperty("ossrhPassword")?.toString()
                }
            }
        }
        project.pluginManager.withPlugin("signing") {
            val signing = project.extensions.getByName("signing") as SigningExtension
            signing.sign(publishing.publications.getByName(moduleName))
        }
        project.pluginManager.withPlugin("java") {
            val java = project.extensions.getByName("java") as JavaPluginExtension
            java.withSourcesJar()
            java.withJavadocJar()
            project.tasks.getByName("javadoc", Javadoc::class) {
                options.encoding = "UTF-8"
            }
        }
    }
}