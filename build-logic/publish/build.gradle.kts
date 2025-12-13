plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(project(":versioning"))
    implementation(kotlin("gradle-plugin"))
    implementation(libs.gradlePlugin.publishPlugin)
}
