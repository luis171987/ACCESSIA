plugins { id("com.android.application") }
android {
    namespace = "org.accessia.browser"
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }
    defaultConfig {
        applicationId = "org.accessia.browser"
        manifestPlaceholders["appLabel"] = "ACCESSIA"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0-dev"
    }
    // La APK debug es una app independiente para preservar la instalación previa.
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".beta"
            manifestPlaceholders["appLabel"] = "ACCESSIA Beta"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    // GeckoView Nightly publicado el 13-Oct-2026; versión de prototipo.
    // PIN must be tested and updated to a reviewed stable release before distribution.
    implementation("org.mozilla.geckoview:geckoview-nightly:160.0.20261013094244")
}
