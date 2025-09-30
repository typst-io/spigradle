package io.typst.spigradle

import groovy.lang.GroovyObject
import io.typst.spigradle.Dependencies
import io.typst.spigradle.Repositories
import io.typst.spigradle.SpigradlePlugin
import io.typst.spigradle.bungee.BungeeDependencies
import io.typst.spigradle.bungee.BungeePlugin
import io.typst.spigradle.bungee.BungeeRepositories
import io.typst.spigradle.nukkit.NukkitDependencies
import io.typst.spigradle.nukkit.NukkitPlugin
import io.typst.spigradle.nukkit.NukkitRepositories
import io.typst.spigradle.spigot.SpigotDependencies
import io.typst.spigradle.spigot.SpigotPlugin
import io.typst.spigradle.spigot.SpigotRepositories
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.repositories
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DependencyResolutionTest {
    lateinit var testProject: Project

    @BeforeTest
    fun setup() {
        testProject = ProjectBuilder.builder().build()
    }

    fun validate(dependencies: List<String>, repositories: List<String>, plugin: Class<*>) {
        testProject.pluginManager.apply("java")
        testProject.pluginManager.apply(plugin)
        testProject.repositories {
            this as GroovyObject
            for (repo in repositories) {
                invokeMethod(repo, emptyArray<Any>())
            }
        }
        testProject.dependencies.apply {
            this as GroovyObject
            for (dep in dependencies) {
                add("compileOnly", invokeMethod(dep, emptyArray<Any>()), closureOf<ExternalModuleDependency> {
                    isTransitive = false
                })
            }
        }
        // https://docs.gradle.org/current/userguide/dependency_resolution.html#sec:programmatic_api
        val compileOnlyConfig = testProject.configurations["compileOnly"]
        compileOnlyConfig.isCanBeResolved = true
        compileOnlyConfig.incoming.resolutionResult.allDependencies.forEach {
            val name = it.toString()
            assertTrue("Couldn't resolved dependency: $name") {
                name !in dependencies || it is ResolvedDependencyResult
            }
        }
    }

    @Test
    fun `validate spigot dependencies`() {
        validate(
            SpigotDependencies.values().map {
                it.alias
            },
            SpigotRepositories.values().map {
                it.alias
            },
            SpigotPlugin::class.java
        )
    }

    @Test
    fun `validate bungee dependencies`() {
        validate(
            BungeeDependencies.values().map {
                it.alias
            },
            BungeeRepositories.values().map {
                it.alias
            },
            BungeePlugin::class.java
        )
    }

    @Test
    fun `validate nukkit dependencies`() {
        validate(
            NukkitDependencies.values().map {
                it.alias
            },
            NukkitRepositories.values().map {
                it.alias
            },
            NukkitPlugin::class.java
        )
    }

    @Test
    fun `validate common dependencies`() {
        validate(
            Dependencies.values().map {
                it.alias
            },
            Repositories.values().map {
                it.alias
            },
            SpigradlePlugin::class.java
        )
    }
}
