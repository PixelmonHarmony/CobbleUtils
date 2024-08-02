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
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}")
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionFabric")) { isTransitive = false }

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
    implementation("org.mongodb:mongodb-driver-reactivestreams:5.1.2")

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
    shadowCommon("org.mongodb:mongodb-driver-reactivestreams:5.1.2")
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
    base.archivesName.set("${project.property("mod_version")}/${project.property("archives_base_name")}-fabric")
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

        relocate("org.reactivestreams", "com.kingpixel.cobbleutils.reactivestreams")
        relocate("com.mongodb", "com.kingpixel.cobbleutils.mongodb")
        relocate("org.bson", "com.kingpixel.cobbleutils.bson")
        relocate("net.kyori", "com.kingpixel.cobbleutils.kyori")
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
