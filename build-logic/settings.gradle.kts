rootProject.name = "build-logic"

include("docs", "publish", "catalog", "central-publish", "java", "bom")

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
