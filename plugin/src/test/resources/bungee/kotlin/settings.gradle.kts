dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    versionCatalogs {
        create("bungees") {
            from("io.typst:bungee-catalog:1.0.0")
        }
    }
}

rootProject.name = "SpigradleBungeeTest"