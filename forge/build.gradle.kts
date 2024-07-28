plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    forge()
}

configurations {
    create("common")
    create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentForge").extendsFrom(configurations["common"])
}

loom {

    enableTransitiveAccessWideners.set(true)
    silentMojangMappingsLicense()


}

dependencies {
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    // Forge
    forge("net.minecraftforge:forge:${property("forge_version")}")

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionForge")) { isTransitive = false }

    modImplementation("com.cobblemon:forge:${property("cobblemon_version")}")

    implementation("thedarkcolour:kotlinforforge:4.4.0")

    listOf(
        "net.kyori:adventure-api:4.14.0",
        "net.kyori:adventure-key:4.14.0",
        "net.kyori:adventure-text-serializer-plain:4.14.0",
        "net.kyori:adventure-text-serializer-legacy:4.14.0",
        "net.kyori:adventure-text-minimessage:4.14.0",
        "net.kyori:examination-api:1.3.0",
        "net.kyori:examination-string:1.3.0",
        "net.kyori:adventure-nbt:4.14.0",
        "net.kyori:adventure-text-serializer-json:4.14.0",
        "net.kyori:adventure-text-logger-slf4j:4.14.0",
        "net.kyori:adventure-platform-api:4.3.0",
        "net.kyori:adventure-text-serializer-ansi:4.14.0",
        "net.kyori:adventure-text-serializer-gson:4.14.0",

        ).forEach { include(it) }


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
        exclude("generations/gg/generations/core/generationscore/forge/datagen/**")
        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }

    jar.get().archiveClassifier.set("dev")
}