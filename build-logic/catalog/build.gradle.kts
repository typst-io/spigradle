plugins {
    `kotlin-dsl`
    `version-catalog`
    `maven-publish`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("io.typst:spigradle-common")
    implementation(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins {
        register("spigradleCatalog") {
            id = "io.typst.spigradle.catalog"
            implementationClass = "io.typst.spigradle.SpigradleCatalogPlugin"
        }
    }
}