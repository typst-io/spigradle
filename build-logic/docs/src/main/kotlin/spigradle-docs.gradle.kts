import org.apache.tools.ant.filters.ConcatFilter

plugins {
    java
    id("org.jetbrains.dokka")
}

dokka {
    moduleName = project.name

    dokkaSourceSets.named("main") {
        jdkVersion = 17

        sourceLink {
            localDirectory.set(rootDir)
            remoteUrl("https://github.com/typst-io/spigradle/tree/master")
            remoteLineSuffix.set("#L")
        }

        externalDocumentationLinks {
            register("groovy-docs") {
                url("https://docs.groovy-lang.org/latest/html/gapi/")
                packageListUrl("https://docs.groovy-lang.org/latest/html/gapi/package-list")
            }
            register("gradle-docs") {
                url("https://docs.gradle.org/current/javadoc/")
                packageListUrl("https://docs.gradle.org/current/javadoc/element-list")
            }
            register("jackson-dataformat-yaml-docs") {
                url("https://javadoc.io/doc/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml/latest/")
                packageListUrl("https://javadoc.io/doc/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml/latest/package-list")
            }
            register("spigot-docs") {
                url("https://hub.spigotmc.org/javadocs/spigot/")
                packageListUrl("https://hub.spigotmc.org/javadocs/spigot/element-list")
            }
        }
    }
}

tasks {
    val spigradleVersion = project.providers.gradleProperty("spigradle.version")
    val updateTemplateDocs by registering {
        group = "spigradle build"
        val docsDir = project.rootDir.resolve("docs")
        fun CopySpec.configure() {
            expand(
                "GRADLE_VERSION" to gradle.gradleVersion,
                "SPIGRADLE_VERSION" to spigradleVersion,
                "KOTLIN_VERSION" to "2.2.20",
                "SHADOW_JAR_VERSION" to "9.2.2",
                "IDEA_EXT_VERSION" to "1.3",
            )
            filter<ConcatFilter>(
                mapOf(
                    "prepend" to docsDir.resolve("header").resolve("header.md")
                )
            )
            rename { name ->
                name.replace(Regex("^template_"), "")
            }
        }
        doLast {
            copy {
                from(docsDir.resolve("templates"))
                include("*.md")
                into(docsDir)
                configure()
            }
            copy {
                from(docsDir.resolve("root-templates"))
                include("*.md")
                into(layout.projectDirectory)
                configure()
            }
        }
    }

    javadoc {
        dependsOn(updateTemplateDocs)
    }
}