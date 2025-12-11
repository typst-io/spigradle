import java.net.URL
import java.util.*

plugins {
    `kotlin-dsl`
    `version-catalog`
    `maven-publish`
    signing
    id("spigradle-versioning")
}

group = "io.typst"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            pom {
                name.set("${project.group}:${project.name}")
                description.set("Spigradle common dependency.")
                url.set("https://github.com/typst-io/spigradle")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("entrypointkr")
                        name.set("Junhyung Im")
                        email.set("entrypointkr@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/typst-io/spigradle.git")
                    developerConnection.set("scm:git:ssh://github.com:typst-io/spigradle.git")
                    url.set("https://github.com/typst-io/spigradle/tree/master")
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatypeReleases"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername")?.toString()
                password = findProperty("ossrhPassword")?.toString()
            }
        }
    }
    signing {
        sign(publishing.publications[project.name])
    }
    java {
        withSourcesJar()
        withJavadocJar()
    }
    tasks.javadoc {
        options.encoding = "UTF-8"
    }
}

tasks.register("publishCentralPortal") {
    group = "publishing"
    doLast {
        val url = URL("https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/${project.group}")
        println(url)
        val con = url.openConnection() as java.net.HttpURLConnection
        val username = findProperty("ossrhUsername")?.toString()
        val password = findProperty("ossrhPassword")?.toString()
        val credential = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        val authValue = "Bearer $credential"
        con.requestMethod = "POST"
        con.setRequestProperty("Authorization", authValue)
        println(con.responseCode)
    }
}