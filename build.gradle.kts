import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.*

plugins {
    kotlin("jvm") version "1.4.20"
    id("org.jetbrains.compose") version "0.3.0-build133"
}

repositories {
    jcenter()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "com.thomaskuenneth.tkdupefinder.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "TKDupeFinder"
            version = "0.1-SNAPSHOT"
            description = "Find duplicate files"
            copyright = "© 2020 Thomas Kuenneth. All rights reserved."
            vendor = "Thomas Kuenneth"
        }
    }
}