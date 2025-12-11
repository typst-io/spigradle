plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(project(":versioning"))
    implementation(kotlin("gradle-plugin"))
    implementation(libs.gradlePlugin.publishPlugin)
}
