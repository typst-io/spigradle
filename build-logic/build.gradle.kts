import java.util.*

// NOTE: https://github.com/gradle/gradle/issues/21144
val rootProperties: Properties = Properties().apply {
    load(file("../gradle.properties").bufferedReader())
}

allprojects {
    group = "io.typst"

    pluginManager.withPlugin("java") {
        val java = extensions["java"] as JavaPluginExtension
        java.toolchain {
            languageVersion.set(JavaLanguageVersion.of(rootProperties.getProperty("java.version").toInt()))
        }
    }
}