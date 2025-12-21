import org.apache.tools.ant.filters.ConcatFilter

plugins {
    java
    id("org.jetbrains.dokka")
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:versioning-plugin:2.1.0")
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
    val spigotCatalogVersion = project.providers.gradleProperty("catalog.spigot.version")
    val bungeeCatalogVersion = project.providers.gradleProperty("catalog.bungee.version")
    val nukkitCatalogVersion = project.providers.gradleProperty("catalog.nukkit.version")
    val commonCatalogVersion = project.providers.gradleProperty("catalog.common.version")
    val updateTemplateDocs by registering {
        group = "spigradle build"
        val rootDir = project.rootDir
        val docsDir = rootDir.resolve("docs")
        fun CopySpec.configure() {
            expand(
                "GRADLE_VERSION" to gradle.gradleVersion,
                "SPIGRADLE_VERSION" to spigradleVersion.get(),
                "KOTLIN_VERSION" to "2.2.20",
                "SHADOW_JAR_VERSION" to "9.2.2",
                "IDEA_EXT_VERSION" to "1.3",
                "SPIGOT_CATALOG_VERSION" to spigotCatalogVersion.get(),
                "BUNGEE_CATALOG_VERSION" to bungeeCatalogVersion.get(),
                "NUKKIT_CATALOG_VERSION" to nukkitCatalogVersion.get(),
                "COMMON_CATALOG_VERSION" to commonCatalogVersion.get(),
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
                into(rootDir)
                configure()
            }
        }
    }

    javadoc {
        dependsOn(updateTemplateDocs)
    }
}