plugins {
    id("java")
    id("java-library")
    kotlin("jvm") version ("1.9.0")

    id("dev.architectury.loom") version ("1.6-SNAPSHOT") apply false
    id("architectury-plugin") version ("3.4-SNAPSHOT") apply false

}

group = "${property("maven_group")}"

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")


    version = "${property("mod_version")}"
    group = "${property("maven_group")}"



    repositories {
        mavenCentral()
        maven(url = "https://maven.impactdev.net/repository/development")
        maven(url = "https://cursemaven.com")
        maven(url = "https://thedarkcolour.github.io/KotlinForForge/")
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.architectury.dev/")
        maven(url = "https://repo.maven.apache.org/maven2/")
        maven(url = "https://repo.spongepowered.org/maven/")
        maven(url = "https://files.minecraftforge.net/maven/")
        maven(url = "https://papermc.io/repo/repository/maven-public/")
        maven("https://maven.impactdev.net/repository/development")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
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
}

