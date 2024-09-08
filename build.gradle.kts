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
}

