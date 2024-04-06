plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

base {
    archivesName = project.property("archives_base_name").toString()
}

repositories {
    maven {
        name = "Curse Maven"
        url = uri("https://cursemaven.com")

        content {
            includeGroup("curse.maven")
        }
    }
}

val shade: Configuration by configurations.creating

dependencies {
    // Minecraft, Fabric
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    // Common
    implementation(project(":common"))
    shade(project(":common"))

    // Open Parties and Claims
    modCompileOnly("curse.maven:open-parties-and-claims-636608:4982662")
}

tasks {
    loom {
        accessWidenerPath.set(file("src/main/resources/lightmap.accesswidener"))
    }

    processResources {
        val properties = mapOf(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version")
        )

        inputs.properties(properties)

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    shadowJar {
        configurations = listOf(shade)
        archiveClassifier.set("dev")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
    }

    jar {
        from("../LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}"}
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 17
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }
}
