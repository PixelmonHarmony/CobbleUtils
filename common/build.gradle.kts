plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")

}
architectury {
    common("forge", "fabric")
    platformSetupLoomIde()
}

dependencies {

    //minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    //mappings(loom.officialMojangMappings())
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")

    modCompileOnly("com.cobblemon:mod:${property("cobblemon_version")}")
    // alL fabric dependencies:
    modCompileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    modImplementation("dev.architectury:architectury:${property("architectury_version")}")
    modImplementation("ca.landonjw.gooeylibs:api:${property("gooeylibs_version")}")

    // Fabric API

    // Forge API
    api("net.minecraftforge:forge:${property("forge_version")}")

    // PlaceholderAPI
    modImplementation("eu.pb4:placeholder-api:${property("placeholder_api_version_fabricandforge")}")
    modImplementation("me.clip:placeholderapi:${property("placeholder_api_version_spigot")}")

    // Database
    api("org.mongodb:mongodb-driver-sync:${property("mongodb_version")}")

    // Lombok
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    implementation("org.projectlombok:lombok:1.18.20")

    // Permissions
    api("net.luckperms:api:${property("luckperms_version")}")
    modImplementation(files("libs/fabric-permissions-api-0.3.1.jar"))
    // Economy
    api("net.impactdev.impactor.api:economy:${property("impactor_version")}")
    api(files("libs/BlanketEconomy-1.1.jar"))

    // Websocket
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    // Kyori Adventure

    /*api("net.kyori:adventure-text-serializer-gson:${property("kyori_version")}")
    api("net.kyori:adventure-api:${property("kyori_version")}")
    api("net.kyori:adventure-key:${property("kyori_version")}")
    api("net.kyori:adventure-text-serializer-plain:${property("kyori_version")}")
    api("net.kyori:adventure-text-serializer-legacy:${property("kyori_version")}")
    api("net.kyori:adventure-text-minimessage:${property("kyori_version")}")
    api("net.kyori:examination-api:1.3.0")
    api("net.kyori:examination-string:1.3.0")
    api("net.kyori:adventure-nbt:${property("kyori_version")}")
    api("net.kyori:adventure-text-serializer-json:${property("kyori_version")}")
    api("net.kyori:adventure-text-logger-slf4j:${property("kyori_version")}")
    api("net.kyori:adventure-platform-api:4.3.0")
    api("net.kyori:adventure-text-serializer-ansi:${property("kyori_version")}")
    api("net.kyori:adventure-text-serializer-gson:${property("kyori_version")}")*/
    api("net.kyori:adventure-api:${property("kyori_version")}")
    api("net.kyori:adventure-nbt:${property("kyori_version")}")
    api("net.kyori:adventure-text-serializer-plain:${property("kyori_version")}")
    api("net.kyori:adventure-text-serializer-legacy:${property("kyori_version")}")
    api("net.kyori:adventure-text-serializer-gson:${property("kyori_version")}")
    api("net.kyori:adventure-text-minimessage:${property("kyori_version")}")
    api("net.kyori:adventure-text-logger-slf4j:${property("kyori_version")}")
    api("net.kyori:adventure-platform-api:4.3.0")
    api("net.kyori:event-api:5.0.0-SNAPSHOT")
}

