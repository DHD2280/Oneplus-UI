plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "it.dhd.oneplusui"
    compileSdk = 35

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.preferencex)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }

            groupId = "it.dhd"
            artifactId = "oneplusui"
            version = "1.3.2"
        }
    }
}