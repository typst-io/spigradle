plugins {
    id("com.gradle.plugin-publish")
    java
    `maven-publish`
}

val spigradleVcsUrl = "https://github.com/typst-io/spigradle.git"

val spigradleDocsJar by tasks.registering(Jar::class) {
    group = "spigradle build"
    archiveClassifier.set("docs")
    from("$projectDir/docs") {
        include("*.md")
    }
    from("$projectDir/CHANGELOG.md")
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "io.typst"
            artifactId = "spigradle"
            artifact(spigradleDocsJar)
        }
    }
}

gradlePlugin {
    website = "https://github.com/typst-io/spigradle"
    vcsUrl = spigradleVcsUrl
    fun formatDesc(name: String) = "An intelligent Gradle plugin for developing $name plugin."
//
    plugins {
        create("spigotBase") {
            id = "io.typst.spigradle.spigot-base"
            displayName = "Spigradle Spigot base plugin."
            description = "The Spigot base plugin."
            tags = listOf("minecraft", "paper", "spigot", "bukkit")
            implementationClass = "io.typst.spigradle.spigot.SpigotBasePlugin"
        }
        create("spigot") {
            id = "io.typst.spigradle.spigot"
            displayName = "Spigradle Spigot plugin"
            description = formatDesc("Spigot")
            tags = listOf("minecraft", "paper", "spigot", "bukkit")
            implementationClass = "io.typst.spigradle.spigot.SpigotPlugin"
        }
//        create("paperBase") {
//            id = "io.typst.spigradle.paper-base"
//            displayName = "Spigradle Paper base plugin."
//            description = "The Paper base plugin."
//            tags = listOf("minecraft", "paper", "spigot", "bukkit")
//            implementationClass = "io.typst.spigradle.paper.PaperBasePlugin"
//        }
//        create("paper") {
//            id = "io.typst.spigradle.paper"
//            displayName = "Spigradle Paper plugin"
//            description = formatDesc("Paper")
//            tags = listOf("minecraft", "paper", "spigot", "bukkit")
//            implementationClass = "io.typst.spigradle.paper.PaperPlugin"
//        }
        create("bungeeBase") {
            id = "io.typst.spigradle.bungee-base"
            displayName = "Spigradle BungeeCord base plugin"
            description = "The BungeeCord base plugin."
            tags = listOf("minecraft", "bungeecord")
            implementationClass = "io.typst.spigradle.bungee.BungeeBasePlugin"
        }
        create("bungee") {
            id = "io.typst.spigradle.bungee"
            displayName = "Spigradle Bungeecord plugin"
            description = formatDesc("Bungeecord")
            tags = listOf("minecraft", "bungeecord")
            implementationClass = "io.typst.spigradle.bungee.BungeePlugin"
        }
        create("nukkit") {
            id = "io.typst.spigradle.nukkit"
            displayName = "Spigradle NukkitX plugin"
            description = formatDesc("NukkitX")
            tags = listOf("minecraft", "nukkit", "nukkitX")
            implementationClass = "io.typst.spigradle.nukkit.NukkitPlugin"
        }
        create("nukkitBase") {
            id = "io.typst.spigradle.nukkit-base"
            displayName = "Spigradle Nukkit base plugin"
            description = "The Nukkit base plugin."
            tags = listOf("minecraft", "nukkit", "nukkitX")
            implementationClass = "io.typst.spigradle.nukkit.NukkitBasePlugin"
        }
    }
}