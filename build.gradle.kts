import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.*

plugins {
    kotlin("jvm") version "1.4.21"
    id("org.jetbrains.compose") version "0.3.0-build139"
}

repositories {
    jcenter()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenLocal()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("com.github.tkuenneth:nativeparameterstoreaccess:0.1.2")
}

compose.desktop {
    application {
        mainClass = "com.thomaskuenneth.tkdupefinder.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg,
                    TargetFormat.Msi)
            packageName = "TKDupeFinder"
            version = "0.1-SNAPSHOT"
            description = "Find duplicate files"
            copyright = "© 2020 Thomas Kuenneth. All rights reserved."
            vendor = "Thomas Kuenneth"
            macOS {
                iconFile.set(project.file("app_icon.icns"))
            }
            windows {
                iconFile.set(project.file("artwork/app:icon.ico"))
            }
        }
    }
}