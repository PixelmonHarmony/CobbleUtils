plugins {
    id("java")
    id("java-library")
    kotlin("jvm") version "1.9.0"
    `maven-publish`
    id("dev.architectury.loom") version "1.6-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
}

group = "${property("maven_group")}"



java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://cursemaven.com")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://jitpack.io")
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://repo.spongepowered.org/maven/")
        maven("https://files.minecraftforge.net/maven/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://maven.impactdev.net/repository/development")
        maven("https://repo.essentialsx.net/releases/")
        maven("https://maven.impactdev.net/repository/development/")
        mavenLocal()

        maven("https://maven.nucleoid.xyz/") {
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("CobbleUtils")
                description.set("A library for Minecraft servers")
                url.set("https://github.com/zonary123/CobbleUtils")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("zonary123")
                        name.set("zonary123")
                        email.set("carlosvarasalonso10@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/zonary123/CobbleUtils")
                    connection.set("scm:git:git://github.com/zonary123/CobbleUtils.git")
                    developerConnection.set("scm:git:ssh://github.com/zonary123/CobbleUtils.git")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/zonary123/CobbleUtils")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

