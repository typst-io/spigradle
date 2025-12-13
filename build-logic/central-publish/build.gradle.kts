plugins {
    `kotlin-dsl`
    `maven-publish`
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":versioning"))
    implementation(kotlin("gradle-plugin"))
    implementation(libs.apache.commons.text)
}

gradlePlugin {
    plugins {
        register("spigradleCentralPublish") {
            id = "io.typst.spigradle.central.publish"
            implementationClass = "io.typst.spigradle.SpigradleCentralPublishPlugin"
        }
    }
}