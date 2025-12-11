package io.typst.spigradle

import groovy.lang.GroovyObject
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
import org.gradle.kotlin.dsl.repositories
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DependencyResolutionTest {
    lateinit var testProject: Project

    @BeforeTest
    fun setup() {
        testProject = ProjectBuilder.builder()
            .build()
    }

    fun validate(dependencies: List<String>, repositories: List<String>, plugin: Class<*>) {
        testProject.pluginManager.apply("java")
        testProject.pluginManager.apply(plugin)
        testProject.repositories {
            this as GroovyObject
            mavenCentral()
            for (repo in repositories) {
                invokeMethod(repo, emptyArray<Any>())
            }
        }

        val depObjects = dependencies.map { name ->
            val dependencies = testProject.dependencies
            dependencies as GroovyObject
            val notation = dependencies.invokeMethod(name, emptyArray<Any>())
            testProject.dependencies.create(notation).apply {
                if (this is ExternalModuleDependency) {
                    isTransitive = false
                }
            }
        }.toTypedArray()

        val detachedConfig = testProject.configurations.detachedConfiguration(*depObjects)
        for (file in detachedConfig.resolve()) {
            println(file.name)
        }

        detachedConfig.incoming.resolutionResult.allDependencies.forEach {
            val name = it.toString()
            println(it.toString())
            assertTrue("Couldn't resolved dependency: $name") {
                it is ResolvedDependencyResult
            }
        }
    }

    @Test
    fun `validate spigot dependencies`() {
        val apis = setOf(
            SpigotDependencies.PURPUR.alias,
            SpigotDependencies.PAPER_API.alias,
            SpigotDependencies.SPIGOT_API.alias
        )
        val baseDeps = SpigotDependencies.entries.filter {
            it.alias !in apis && !it.local
        }

        // base
        validate(
            baseDeps.map { it.alias },
            SpigotRepositories.entries.map { it.alias },
            SpigotPlugin::class.java
        )

        // purpur
        validate(
            listOf(SpigotDependencies.PURPUR.alias),
            SpigotRepositories.entries.map {
                it.alias
            },
            SpigotPlugin::class.java
        )
        // spigot
        validate(
            listOf(SpigotDependencies.SPIGOT_API.alias),
            SpigotRepositories.entries.map {
                it.alias
            },
            SpigotPlugin::class.java
        )
        // paper
        validate(
            listOf(SpigotDependencies.PAPER_API.alias),
            SpigotRepositories.entries.map {
                it.alias
            },
            SpigotPlugin::class.java
        )
    }

    @Test
    fun `validate bungee dependencies`() {
        validate(
            BungeeDependencies.entries
                .filter { !it.local }
                .map {
                    it.alias
                },
            BungeeRepositories.entries.map {
                it.alias
            },
            BungeePlugin::class.java
        )
    }

    @Test
    fun `validate nukkit dependencies`() {
        validate(
            NukkitDependencies.entries
                .filter { !it.local }
                .map {
                    it.alias
                },
            NukkitRepositories.entries.map {
                it.alias
            },
            NukkitPlugin::class.java
        )
    }

    @Test
    fun `validate common dependencies`() {
        validate(
            Dependencies.entries
                .map {
                    it.alias
                },
            Repositories.entries.map {
                it.alias
            },
            SpigradlePlugin::class.java
        )
    }
}
