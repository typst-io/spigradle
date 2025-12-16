package io.typst.spigradle

import groovy.lang.GroovyObject
import io.typst.spigradle.bungee.BungeePlugin
import io.typst.spigradle.bungee.BungeeRepositories
import io.typst.spigradle.catalog.BungeeDependencies
import io.typst.spigradle.catalog.CommonDependencies
import io.typst.spigradle.catalog.NukkitDependencies
import io.typst.spigradle.catalog.SpigotDependencies
import io.typst.spigradle.nukkit.NukkitPlugin
import io.typst.spigradle.nukkit.NukkitRepositories
import io.typst.spigradle.spigot.SpigotPlugin
import io.typst.spigradle.spigot.SpigotRepositories
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.kotlin.dsl.maven
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
                maven(repo)
            }
        }

        val depObjects = dependencies.map { notation ->
            val dependencies = testProject.dependencies
            dependencies as GroovyObject
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
        val apis = listOf(
            SpigotDependencies.PURPUR.dependency,
            SpigotDependencies.PAPER_API.dependency,
            SpigotDependencies.SPIGOT_API.dependency
        ).associateBy { it.alias }
        val baseDeps = SpigotDependencies.entries.filter {
            it.dependency.alias !in apis && !it.dependency.isLocal
        }

        // base
        validate(
            baseDeps.map { it.dependency.format() },
            SpigotRepositories.entries.map { it.address },
            SpigotPlugin::class.java
        )

        // purpur
        validate(
            listOf(SpigotDependencies.PURPUR.dependency.format()),
            SpigotRepositories.entries.map {
                it.address
            },
            SpigotPlugin::class.java
        )
        // spigot
        validate(
            listOf(SpigotDependencies.SPIGOT_API.dependency.format()),
            SpigotRepositories.entries.map {
                it.address
            },
            SpigotPlugin::class.java
        )
        // paper
        validate(
            listOf(SpigotDependencies.PAPER_API.dependency.format()),
            SpigotRepositories.entries.map {
                it.address
            },
            SpigotPlugin::class.java
        )
    }

    @Test
    fun `validate bungee dependencies`() {
        validate(
            BungeeDependencies.entries
                .flatMap {
                    if (!it.dependency.isLocal) {
                        listOf(it.dependency.format())
                    } else emptyList()
                },
            BungeeRepositories.entries.map {
                it.address
            },
            BungeePlugin::class.java
        )
    }

    @Test
    fun `validate nukkit dependencies`() {
        validate(
            NukkitDependencies.entries
                .flatMap {
                    if (!it.dependency.isLocal) {
                        listOf(it.dependency.format())
                    } else emptyList()
                },
            NukkitRepositories.entries.map {
                it.address
            },
            NukkitPlugin::class.java
        )
    }

    @Test
    fun `validate common dependencies`() {
        validate(
            CommonDependencies.entries
                .flatMap {
                    if (!it.dependency.isLocal) {
                        listOf(it.dependency.format())
                    } else emptyList()
                },
            Repositories.entries.map {
                it.alias
            },
            SpigotPlugin::class.java
        )
    }
}
