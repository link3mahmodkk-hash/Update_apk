import JSZip from 'jszip';

export async function downloadAndroidProjectZip() {
  const zip = new JSZip();

  // Root gradle files
  zip.file('settings.gradle.kts', `pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "ParsPingVPN"
include(":app")
`);

  zip.file('build.gradle.kts', `plugins {
    id("com.android.application") version "8.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
`);

  zip.file('gradle.properties', `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
android.nonTransitiveRClass=true
`);

  zip.file('gradle/wrapper/gradle-wrapper.properties', `distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
`);

  // GitHub Actions workflow
  zip.file('.github/workflows/android-build.yml', `name: Build Pars Ping Android APK

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    name: Build & Sign Pars Ping APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Grant Execute Permission for Gradlew
        run: chmod +x ./gradlew || true

      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest --no-daemon

      - name: Build Debug APK
        run: ./gradlew assembleDebug --no-daemon --stacktrace

      - name: Upload Pars Ping APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: ParsPing-VPN-Debug-APK
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 14
`);

  // App module build.gradle.kts
  zip.file('app/build.gradle.kts', `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.parsping.vpn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.parsping.vpn"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")

    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("com.google.code.gson:gson:2.10.1")

    // Official WireGuard Android Tunnel Library
    implementation("com.wireguard.android:tunnel:1.0.20230707")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}
`);

  // Manifest & Resources
  zip.file('app/src/main/AndroidManifest.xml', `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:label="Pars Ping VPN"
        android:supportsRtl="true"
        android:theme="@style/Theme.ParsPingVPN">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".net.vpn.ParsPingVpnService"
            android:permission="android.permission.BIND_VPN_SERVICE"
            android:exported="false">
            <intent-filter>
                <action android:name="android.net.VpnService" />
            </intent-filter>
        </service>
    </application>
</manifest>
`);

  zip.file('app/src/main/res/values/strings.xml', `<resources>
    <string name="app_name">Pars Ping VPN</string>
    <string name="custom_theme">تم دلخواه</string>
    <string name="ping_all">پینگ همه</string>
</resources>
`);

  zip.file('app/src/main/res/values/themes.xml', `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.ParsPingVPN" parent="android:Theme.Material.NoActionBar">
        <item name="android:statusBarColor">#030303</item>
        <item name="android:navigationBarColor">#030303</item>
    </style>
</resources>
`);

  // Persian README
  zip.file('README.md', `# پروژه اندروید پارس پینگ وی‌پی‌ان (Pars Ping VPN)
این پروژه کامل سورس‌کد اندروید به زبان کاتلین و جت‌پک کامپوز است.

## نحوه بیلد APK با گیت‌هاب اکشنز (فقط با گوشی):
۱. تمام فایل‌های این زیپ را در ریپوزیتوری گیت‌هاب خود آپلود یا push کنید.
۲. وارد تب Actions در گیت‌هاب شوید.
۳. ورک‌فلو «Build Pars Ping Android APK» به طور خودکار شروع به کامپایل پروژه می‌کند.
۴. پس از اتمام (حدود ۲ الی ۳ دقیقه)، وارد آخرین اجرا شوید و فایل «ParsPing-VPN-Debug-APK.zip» را مستقیماً دانلود و نصب کنید.
`);

  const blob = await zip.generateAsync({ type: 'blob' });
  const downloadUrl = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = downloadUrl;
  a.download = 'ParsPing-VPN-Android-FullSource.zip';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(downloadUrl);
}
