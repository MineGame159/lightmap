plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

base {
    archivesName = project.property("archives_base_name").toString() + "-common"
}

val shade: Configuration by configurations.creating

dependencies {
    // NBT
    implementation("meteordevelopment:NBT:${project.property("nbt_version")}")
    shade("meteordevelopment:NBT:${project.property("nbt_version")}")

    // MicroHTTP
    implementation("org.microhttp:microhttp:${project.property("microhttp_version")}")
    shade("org.microhttp:microhttp:${project.property("microhttp_version")}")

    // Compile only
    compileOnly("org.slf4j:slf4j-api:2.0.1")
    compileOnly("it.unimi.dsi:fastutil:8.5.9")
    compileOnly("com.google.code.gson:gson:2.10.1")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 17
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    shadowJar {
        configurations = listOf(shade)
        archiveClassifier.set("")
    }

    jar {
        archiveClassifier.set("noshade")
    }

    build {
        dependsOn(shadowJar)
    }
}
