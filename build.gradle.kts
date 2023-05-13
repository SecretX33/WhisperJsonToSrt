import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.8.21"
//    id("org.beryx.runtime") version "1.13.0"
    application
}

group = "com.github.secretx33"
version = "1.0-SNAPSHOT"

val javaVersion = 17

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.commons:commons-compress:1.23.0")
    implementation("org.tukaani:xz:1.9")
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.15.0"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

tasks.test { useJUnitPlatform() }

tasks.jar { enabled = false }

artifacts.archives(tasks.shadowJar)

tasks.shadowJar {
    archiveFileName.set("${rootProject.name}.jar")
}

tasks.withType<JavaCompile> {
    options.apply {
        release.set(javaVersion)
        options.encoding = "UTF-8"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = javaVersion.toString()
    }
}

val mainClassName = "com.github.secretx33.whisperjsontosrt.WhisperJsonToSrtKt"

application {
    mainClass.set(mainClassName)
}

//runtime {
//    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
//    modules.set(listOf("java.base"))
//    launcher {
//        jvmArgs = listOf("-Xms1m", "-Xmx256m", "-XX:+UseG1GC", "-XX:+DisableExplicitGC", "-Dfile.encoding=UTF-8")
//    }
//    jpackage {
//        installerType = "app-image"
//        mainJar = tasks.getByName<ShadowJar>("shadowJar").archiveFileName.get()
//        version = "0.1"
//        installerOptions = listOf(
//            "--description", rootProject.description,
//            "--win-menu",
//            "--win-per-user-install",
//            "--win-shortcut"
//        )
//    }
//}

//jlink {
//    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
//    launcher {
//        name = "KotlinPlayground"
//        mainClass.set(mainClassName)
//        jvmArgs = listOf("-Xms1m", "-Xmx256m", "-XX:+UseG1GC", "-XX:+DisableExplicitGC")
//    }
//    jpackage {
//        installerType = appInstallerType
//        installerName = appName
//        appVersion = project.version.toString()
//        if (os.isWindows) {
//            icon = rootProject.file(appIconIco).path
//            installerOptions = listOf(
//                "--description", rootProject.description,
//                "--copyright", appCopyright,
//                "--vendor", appVendor,
//                "--win-dir-chooser",
//                "--win-menu",
//                "--win-per-user-install",
//                "--win-shortcut"
//            )
//        }
//        if (os.isLinux) {
//            icon = rootProject.file(appIconPng).path
//            installerOptions = listOf(
//                "--description", rootProject.description,
//                "--copyright", appCopyright,
//                "--vendor", appVendor,
//                "--linux-shortcut"
//            )
//        }
//        if (os.isMacOsX) {
//            icon = rootProject.file(appIconPng).path
//        }
//    }
//}