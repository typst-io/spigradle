import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.net.URL
import java.util.*

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

allprojects {
    group = "io.typst"

    pluginManager.withPlugin("java") {
        val java = extensions["java"] as JavaPluginExtension
        java.toolchain {
            languageVersion.set(JavaLanguageVersion.of(property("java.version")!!.toString().toInt()))
        }
    }

    pluginManager.withPlugin("kotlin") {
        val kotlin = extensions["kotlin"] as KotlinJvmProjectExtension
        kotlin.compilerOptions {
            apiVersion =
                KotlinVersion.fromVersion(property("kotlin.version")!!.toString())
            languageVersion = KotlinVersion.fromVersion(property("kotlin.version")!!.toString())
        }
    }
}

val ossrhUsername = providers.gradleProperty("ossrh.username")
val ossrhPassword = providers.gradleProperty("ossrh.password")

project.tasks.register("publishCentralPortal") {
    group = "publishing"
    val projectGroup = project.group
    doLast {
        val url = URL("https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/${projectGroup}")
        println(url)
        val con = url.openConnection() as java.net.HttpURLConnection
        val username = ossrhUsername.get()
        val password = ossrhPassword.get()
        val credential = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        val authValue = "Bearer $credential"
        con.requestMethod = "POST"
        con.setRequestProperty("Authorization", authValue)
        println(con.responseCode)
        assert(con.responseCode == 200)
    }
}