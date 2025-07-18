package kr.entree.spigradle

import groovy.lang.GroovyObject
import io.typst.spigradle.Dependencies
import io.typst.spigradle.Repositories
import io.typst.spigradle.applySpigradlePlugin
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

    @Test
    fun `validate dependencies`() {
        val dependencies = Dependencies.ALL.mapNotNull { nameToDep ->
            val (name, dependency) = nameToDep
            if (name != "spigradle" && !dependency.isLocal)
                dependency.format() to nameToDep
            else null
        }.toMap()
        testProject.applySpigradlePlugin()
        testProject.repositories {
            this as GroovyObject
            Repositories.ALL.forEach { (name, _) ->
                invokeMethod(name, emptyArray<Any>())
            }
        }
        testProject.dependencies.apply {
            this as GroovyObject
            dependencies.values.forEach { (name, _) ->
                add("compileOnly", invokeMethod(name, emptyArray<Any>()), closureOf<ExternalModuleDependency> {
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
}