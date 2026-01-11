plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("org.panteleyev.jpackageplugin") version "1.7.3"
     id("com.gradleup.shadow") version "8.3.9"
}


repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.formdev:flatlaf:3.7")

    implementation("com.google.code.gson:gson:2.13.1")

    implementation("com.google.guava:guava:33.4.8-jre")

    implementation("commons-io:commons-io:2.20.0")

    implementation("org.apache.commons:commons-lang3:3.18.0")

    implementation("org.codehaus.plexus:plexus-archiver:4.10.0")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    // Define the main class for the application.
    mainClass = "psptools.App"
    //applicationDefaultJvmArgs = listOf("-Djava.library.path=" + file("${buildDir}/libs/ApolloLib/shared").absolutePath, "--enable-preview")
}


var version = "1.0.1"

tasks.jpackage {
    dependsOn("build", "shadowJar")

    input = layout.buildDirectory.dir("builtJars")
    destination = layout.buildDirectory.dir("builtDist")
    appVersion = version

    appName = "PSPTools"
    vendor = "_FN10_"

    javaOptions = listOf("-Xmx64m", "-Xms16m")

    icon = file("src/main/resources/icon.png")


    type = org.panteleyev.jpackage.ImageType.APP_IMAGE

    mainJar = "PSPTools-$version-all.jar"
    mainClass = "psptools.App"

    windows {
        icon = layout.projectDirectory.file("/src/main/resources/icon.ico")
    }

    //I AM never building for mac, have fun
    mac {
        icon = layout.projectDirectory.file("/src/main/resources/icon.icns")
        macPackageName = "PSPTools"
    }
}

tasks.shadowJar {
    archiveBaseName.set("PSPTools")
    archiveVersion.set(version)
    destinationDirectory.set(layout.buildDirectory.dir("builtJars"))
}