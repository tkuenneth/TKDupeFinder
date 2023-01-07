import org.jetbrains.compose.desktop.application.dsl.*
import java.util.*
import java.io.*

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.compose") version "1.2.1"
}

var buildNumber = 1
val properties = Properties()
val file = rootProject.file("src/main/resources/version.properties")
if (file.isFile) {
    InputStreamReader(FileInputStream(file), Charsets.UTF_8).use { reader ->
        properties.load(reader)
        buildNumber = 1 + properties.getProperty("BUILD_NUMBER", "0").toInt()
        properties.setProperty("BUILD_NUMBER", "$buildNumber")
        FileOutputStream(file).use { outputStream ->
            properties.store(outputStream, null)
        }
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
                bundleID = "eu.thomaskuenneth.tkdupefinder"
                iconFile.set(project.file("artwork/app_icon.icns"))
                signing {
                    sign.set(true)
                    identity.set("Thomas Kuenneth")
                }
                notarization {
                    appleID.set("thomas.kuenneth@icloud.com")
                    password.set("@keychain:NOTARIZATION_PASSWORD")
                }
            }
            windows {
                iconFile.set(project.file("artwork/app_icon.ico"))
                menuGroup = "Thomas Kuenneth"
            }
            modules("java.instrument", "jdk.unsupported")
        }
    }
}
