import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
//val developmentForge: Configuration by configurations.getting

architectury {
    platformSetupLoomIde()
    forge()
}

configurations {
    //create("common")
    //create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentForge").extendsFrom(configurations["common"])
}

loom {
    enableTransitiveAccessWideners.set(true)
    silentMojangMappingsLicense()
}

dependencies {
    //minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    //mappings(loom.officialMojangMappings())
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")

    // Forge
    forge("net.minecraftforge:forge:${property("forge_version")}")

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionForge")) { isTransitive = false }

    modImplementation("com.cobblemon:forge:${property("cobblemon_version")}")

    implementation("thedarkcolour:kotlinforforge:4.4.0")

    implementation("net.kyori:adventure-text-serializer-gson:4.14.0")
    implementation("net.kyori:adventure-api:4.14.0")
    implementation("net.kyori:adventure-key:4.14.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.14.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.14.0")
    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    implementation("net.kyori:examination-api:1.3.0")
    implementation("net.kyori:examination-string:1.3.0")
    implementation("net.kyori:adventure-nbt:4.14.0")
    implementation("net.kyori:adventure-text-serializer-json:4.14.0")
    implementation("net.kyori:adventure-text-logger-slf4j:4.14.0")
    implementation("net.kyori:adventure-platform-api:4.3.0")
    implementation("net.kyori:adventure-text-serializer-ansi:4.14.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.14.0")
    //implementation("org.mongodb:mongodb-driver-reactivestreams:5.1.2")

    shadowCommon("net.kyori:adventure-text-serializer-gson:4.14.0")
    shadowCommon("net.kyori:adventure-api:4.14.0")
    shadowCommon("net.kyori:adventure-key:4.14.0")
    shadowCommon("net.kyori:adventure-text-serializer-plain:4.14.0")
    shadowCommon("net.kyori:adventure-text-serializer-legacy:4.14.0")
    shadowCommon("net.kyori:adventure-text-minimessage:4.14.0")
    shadowCommon("net.kyori:examination-api:1.3.0")
    shadowCommon("net.kyori:examination-string:1.3.0")
    shadowCommon("net.kyori:adventure-nbt:4.14.0")
    shadowCommon("net.kyori:adventure-text-serializer-json:4.14.0")
    shadowCommon("net.kyori:adventure-text-logger-slf4j:4.14.0")
    shadowCommon("net.kyori:adventure-platform-api:4.3.0")
    shadowCommon("net.kyori:adventure-text-serializer-ansi:4.14.0")
    shadowCommon("net.kyori:adventure-text-serializer-gson:4.14.0")
    //shadowCommon("org.mongodb:mongodb-driver-reactivestreams:5.1.2")
}

tasks.processResources {
    filesMatching("META-INF/mods.toml") {
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
    base.archivesName.set("${project.property("mod_version")}/${project.property("archives_base_name")}-forge")
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }
    shadowJar {
        exclude("fabric.mod.json")
        exclude("architectury.common.json")
        exclude("com/google/gson/**/*")
        exclude("org/intellij/**/*")
        exclude("org/jetbrains/**/*")
        exclude("generations/gg/generations/core/generationscore/forge/datagen/**")

        //relocate("org.reactivestreams", "com.kingpixel.cobbleutils.reactivestreams")
        //relocate("com.mongodb", "com.kingpixel.cobbleutils.mongodb")
        relocate("org.bson", "com.kingpixel.cobbleutils.bson")
        relocate("net.kyori", "com.kingpixel.cobbleutils.kyori")
        relocate("org.slf4j", "com.kingpixel.cobbleutils.slf4j")

        transformers.add(ServiceFileTransformer())

        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)

    }

    jar {
        archiveClassifier.set("dev")
    }
}
