import java.net.URL
import java.util.*

plugins {
    `kotlin-dsl`
    id("spigradle-versioning")
}

repositories {
    mavenCentral()
}

subprojects {
    group = "io.typst"
}

project.tasks.register("publishCentralPortal") {
    group = "publishing"
    doLast {
        val url = URL("https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/${project.group}")
        println(url)
        val con = url.openConnection() as java.net.HttpURLConnection
        val username = project.findProperty("ossrhUsername")?.toString()
        val password = project.findProperty("ossrhPassword")?.toString()
        val credential = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        val authValue = "Bearer $credential"
        con.requestMethod = "POST"
        con.setRequestProperty("Authorization", authValue)
        println(con.responseCode)
    }
}