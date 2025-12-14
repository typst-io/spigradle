plugins {
    `kotlin-dsl`
    `version-catalog`
    `maven-publish`
    `java-gradle-plugin`
}

dependencies {
    implementation(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins {
        register("spigradleCatalog") {
            id = "io.typst.spigradle.catalog"
            implementationClass = "io.typst.spigradle.catalog.SpigradleCatalogPublishPlugin"
        }
    }
}
