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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.18.0")
    implementation("org.mozilla.geckoview:geckoview-nightly:160.0.20261013094244")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.mockito:mockito-core:5.20.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}

val copyAgentPrompt by tasks.registering(Copy::class) {
    from("../../shared/agent-prompt.txt")
    from("../../shared/THIRD_PARTY_NOTICES.txt")
    from("../../shared/licenses") { into("licenses") }
    into(layout.buildDirectory.dir("generated/agentAssets"))
}
android.sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/agentAssets").get().asFile)
tasks.named("preBuild") { dependsOn(copyAgentPrompt) }
