plugins { id("com.android.application") }
android {
    namespace = "org.accessia.browser"
    compileSdk = 36
    defaultConfig {
        applicationId = "org.accessia.browser"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-dev"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    // Nightly version matched to GeckoView's published 17-Oct-2026 API docs.
    // PIN must be tested and updated to a reviewed stable release before distribution.
    implementation("org.mozilla.geckoview:geckoview-nightly:160.0.20261017034010")
}
