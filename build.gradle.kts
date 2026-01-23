plugins {
    application
    id("org.panteleyev.jpackageplugin") version "1.7.3"
    id("com.gradleup.shadow") version "8.3.9"
}


repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

tasks.test {
    failOnNoDiscoveredTests = false
}

dependencies {
    implementation("com.formdev:flatlaf:3.7")

    implementation("com.google.code.gson:gson:2.13.1")

    implementation("com.google.guava:guava:33.4.8-jre")

    implementation("commons-io:commons-io:2.20.0")

    implementation("org.apache.commons:commons-lang3:3.18.0")

    implementation("org.codehaus.plexus:plexus-archiver:4.10.0")

    //1.1.0

    // https://mvnrepository.com/artifact/ws.schild/jave-all-deps
    implementation("ws.schild:jave-all-deps:3.5.0")

    // Source: https://mvnrepository.com/artifact/commons-net/commons-net
    implementation("commons-net:commons-net:3.12.0")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    mainClass = "fn10.psptools.App"
}


var version = "1.1.0"

tasks.jpackage {
    dependsOn("build", "shadowJar")

    input = layout.buildDirectory.dir("builtJars")
    destination = layout.buildDirectory.dir("builtDist")
    appVersion = version

    appName = "PSPTools"
    vendor = "_FN10_"

    icon = file("src/main/resources/icon.png")


    type = org.panteleyev.jpackage.ImageType.APP_IMAGE

    mainJar = "PSPTools-$version-all.jar"
    mainClass = "fn10.psptools.App"

    windows {
        icon = layout.projectDirectory.file("/src/main/resources/icon.ico")
    }
}

tasks.shadowJar {
    archiveBaseName.set("PSPTools")
    archiveVersion.set(version)
    destinationDirectory.set(layout.buildDirectory.dir("builtJars"))
}