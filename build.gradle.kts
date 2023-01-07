import org.jetbrains.compose.desktop.application.dsl.*
import java.util.*
import java.io.*

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.compose") version "1.2.1"
}

val properties = Properties()
val file = rootProject.file("src/main/resources/version.properties")
if (file.isFile) {
    InputStreamReader(FileInputStream(file), Charsets.UTF_8).use { reader ->
        properties.load(reader)
    }
} else error("${file.absolutePath} not found")
version = properties.getProperty("VERSION")

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.github.tkuenneth:nativeparameterstoreaccess:0.1.2")
}

compose.desktop {
    application {
        mainClass = "com.thomaskuenneth.tkdupefinder.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg,
                    TargetFormat.Msi)
            packageName = "TKDupeFinder"
            packageVersion = version.toString()
            description = "Find duplicate files"
            copyright = "© 2011 - 2023 Thomas Kuenneth. All rights reserved."
            vendor = "Thomas Kuenneth"
            macOS {
                iconFile.set(project.file("artwork/app_icon.icns"))
            }
            windows {
                iconFile.set(project.file("artwork/app_icon.ico"))
                menuGroup = "Thomas Kuenneth"
            }
            modules("java.instrument", "jdk.unsupported")
        }
    }
}