import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    //create("common")
    //create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentFabric").extendsFrom(configurations["common"])
}

loom {
    enableTransitiveAccessWideners.set(true)
    silentMojangMappingsLicense()


}

dependencies {
    //minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    //mappings(loom.officialMojangMappings())

    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")

    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}")
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionFabric")) { isTransitive = false }

    shadowCommon("net.kyori:adventure-api:${property("kyori_version")}")
    shadowCommon("net.kyori:adventure-nbt:${property("kyori_version")}")
    shadowCommon("net.kyori:adventure-text-serializer-plain:${property("kyori_version")}")
    shadowCommon("net.kyori:adventure-text-serializer-legacy:${property("kyori_version")}")
    shadowCommon("net.kyori:adventure-text-serializer-gson:${property("kyori_version")}")
    shadowCommon("net.kyori:adventure-text-minimessage:${property("kyori_version")}")
    shadowCommon("net.kyori:adventure-text-logger-slf4j:${property("kyori_version")}")
    shadowCommon("net.kyori:adventure-platform-api:4.3.0")
    shadowCommon("net.kyori:event-api:5.0.0-SNAPSHOT")
    shadowCommon("org.mongodb:mongodb-driver-sync:${property("mongodb_version")}")
    shadowCommon("org.java-websocket:Java-WebSocket:1.5.7")
}

tasks.processResources {
    expand(
        mapOf(
            "mod_name" to project.property("mod_name"),
            "mod_id" to project.property("mod_id"),
            "mod_version" to project.property("mod_version"),
            "mod_description" to project.property("mod_description"),
            "author" to project.property("author"),
            "repository" to project.property("repository"),
            "license" to project.property("license"),
            "mod_icon" to project.property("mod_icon"),
            "environment" to project.property("environment"),
            "supported_minecraft_versions" to project.property("supported_minecraft_versions")
        )
    )
}


tasks {
    base.archivesName.set(
        "${project.property("mod_version")}/${project.property("archives_base_name")}-fabric" +
                "-${
                    project.property(
                        "mod_version"
                    )
                }"
    )
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("generations/gg/generations/core/generationscore/fabric/datagen/**")
        exclude("data/forge/**")
        exclude("architectury.common.json")
        exclude("com/google/gson/**/*")
        exclude("org/intellij/**/*")
        exclude("org/jetbrains/**/*")
        // No excluimos el "net/kyori/adventure/key/**" para que siga incluido en el JAR, pero sin relocalizar.

        //relocate("org.reactivestreams", "com.kingpixel.cobbleutils.reactivestreams")
        relocate("com.mongodb", "com.kingpixel.cobbleutils.mongodb")
        relocate("org.bson", "com.kingpixel.cobbleutils.bson")
        relocate("net.kyori", "com.kingpixel.cobbleutils.kyori") {
            exclude("net/kyori/adventure/key/**")
        }
        relocate("org.slf4j", "com.kingpixel.cobbleutils.slf4j")

        transformers.add(ServiceFileTransformer())

        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")
    }



    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }

    jar.get().archiveClassifier.set("dev")
}
