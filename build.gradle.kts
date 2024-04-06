subprojects {
    group = project.property("maven_group").toString()
    version = project.property("mod_version").toString()

    repositories {
        maven {
            name = "Meteor Maven - Releases"
            url = uri("https://maven.meteordev.org/releases")
        }

        mavenCentral()
    }
}
