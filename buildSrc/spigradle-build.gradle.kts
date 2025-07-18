plugins {
    java
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(libs.gradlePlugin.dokka)
    implementation(libs.gradlePlugin.publishPlugin)
}

kotlin {
    compilerOptions {
        // Enable strict compilation mode for build scripts
        allWarningsAsErrors = true
    }
}
