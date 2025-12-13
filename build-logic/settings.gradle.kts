rootProject.name = "build-logic"

include("docs", "publish", "versioning", "catalog", "central-publish", "java")
includeBuild("../common")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
