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
        versionCode = 3
        versionName = "0.3.0-dev"
    }
    // La APK debug es una app independiente para preservar la instalación previa.
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".beta03"
            manifestPlaceholders["appLabel"] = "ACCESSIA Beta 0.3"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}
dependencies {
    // GeckoView Nightly publicado el 13-Oct-2026; versión de prototipo.
    // PIN must be tested and updated to a reviewed stable release before distribution.
    implementation("org.mozilla.geckoview:geckoview-nightly:160.0.20261013094244")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.mockito:mockito-core:5.20.0")
}
