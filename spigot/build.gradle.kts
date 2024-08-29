plugins {
    id("java")
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
architectury {
    platformSetupLoomIde()
    forge()
}
group = "org.kingpixel"
version = "1.0-SNAPSHOT"

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating

configurations {
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    shadowCommon.extendsFrom(configurations["common"])
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    //compileOnly("io.socket:socket.io-server:4.0.1")

    // Socket
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    // Gson
    implementation("com.google.code.gson:gson:2.11.0")
    // Economy
    api("com.github.MilkBowl:VaultAPI:1.7")
    shadowCommon("org.java-websocket:Java-WebSocket:1.5.7")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(
            mapOf(
                "author" to project.property("author"),
                "mod_name" to project.property("mod_name"),
                "mod_id" to project.property("mod_id"),
                "version" to project.property("mod_version"),
                "mod_description" to project.property("mod_description"),
                "repository" to project.property("repository"),
                "license" to project.property("license"),
                "mod_icon" to project.property("mod_icon"),
                "environment" to project.property("environment"),
                "supported_minecraft_versions" to project.property("supported_minecraft_versions")
            )
        )
    }
}

tasks {
    base.archivesName.set("${project.property("mod_version")}/${project.property("archives_base_name")}-Spigot")
    processResources {
        inputs.property("version", project.version)

        filesMatching("plugin.yml") {
            expand(mapOf("version" to project.version))
        }
    }
    // Configuración del JAR principal
    jar {
        archiveClassifier.set("dev")
    }

    // Configuración del Shadow JAR
    shadowJar {
        exclude("fabric.mod.json")
        exclude("architectury.common.json")
        exclude("com/google/gson/**/*")
        exclude("org/intellij/**/*")
        exclude("org/jetbrains/**/*")
        exclude("generations/gg/generations/core/generationscore/forge/datagen/**")

        relocate("com.mongodb", "com.kingpixel.cobbleutils.mongodb")
        relocate("org.bson", "com.kingpixel.cobbleutils.bson")
        relocate("net.kyori", "com.kingpixel.cobbleutils.kyori")
        relocate("org.slf4j", "com.kingpixel.cobbleutils.slf4j")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }
}
