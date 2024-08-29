rootProject.name = "CobbleUtils"

pluginManagement {
    repositories {
        mavenCentral()
        maven("https://cursemaven.com")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
        maven("https://jitpack.io")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://repo.spongepowered.org/maven/")
        maven("https://files.minecraftforge.net/maven/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://maven.impactdev.net/repository/development")
        maven("https://repo.essentialsx.net/releases/")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://maven.nucleoid.xyz/")
            name = "Nucleoid"
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            name = "Sonatype Snapshots"
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots") {
            name = "Sonatype 01 Snapshots"
        }
    }

    listOf(
        "net.kyori:examination-api:1.3.0",
        "net.kyori:examination-string:1.3.0",
        "net.kyori:adventure-api:4.14.0",
        "net.kyori:adventure-key:4.14.0",
        "net.kyori:adventure-nbt:4.14.0",
        "net.kyori:adventure-text-serializer-plain:4.14.0",
        "net.kyori:adventure-text-serializer-legacy:4.14.0",
        "net.kyori:adventure-text-serializer-gson:4.14.0",
        "net.kyori:adventure-text-serializer-json:4.14.0",
        "net.kyori:adventure-text-minimessage:4.14.0",
        "net.kyori:adventure-text-logger-slf4j:4.14.0",
        "net.kyori:event-api:5.0.0-SNAPSHOT",
    ).forEach { include(it) }


}

include("common", "fabric", "forge", "spigot")