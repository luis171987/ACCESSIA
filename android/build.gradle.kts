
// Kotlin compatible con GeckoView.
// Android Gradle Plugin utiliza Kotlin integrado.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(
            "org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21"
        )
    }
}
plugins {
    id("com.android.application") version "9.4.0" apply false
}
