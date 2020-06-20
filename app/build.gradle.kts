plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    jacoco
}

jacoco {
    toolVersion = "0.8.5"
}

project.afterEvaluate {
    // Grab all build types and product flavors
    val buildTypeNames: List<String> = android.buildTypes.map { it.name }
    val productFlavorNames: ArrayList<String> = ArrayList(android.productFlavors.map { it.name })
    // When no product flavors defined, use empty
    if (productFlavorNames.isEmpty()) productFlavorNames.add("")
    productFlavorNames.forEach { productFlavorName ->
        buildTypeNames.forEach { buildTypeName ->
            val sourceName: String
            val sourcePath: String
            if (productFlavorName.isEmpty()) {
                sourcePath = buildTypeName
                sourceName = buildTypeName
            } else {
                sourcePath = "${productFlavorName}/${buildTypeName}"
                sourceName = "${productFlavorName}${buildTypeName.capitalize()}"
            }
            val testTaskName = "test${sourceName.capitalize()}UnitTest"
            task<JacocoReport>("${testTaskName}Coverage") {
                group = "coverage"
                description =
                    "Generate Jacoco coverage reports on the ${sourceName.capitalize()} build."
                val excludeFiles = arrayListOf(
                    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
                    "**/*Test*.*", "android/**/*.*",
                    "**/*_MembersInjector.class",
                    "**/Dagger*Component.class",
                    "**/Dagger*Component\$Builder.class",
                    "**/*_*Factory.class",
                    "**/*ComponentImpl.class",
                    "**/*SubComponentBuilder.class",
                    "**/*Creator.class",
                    "**/*Application*.*",
                    "**/*Activity*.*",
                    "**/*Fragment*.*",
                    "**/*Adapter*.*",
                    "**/*Dialog*.*",
                    "**/*Args*.*",
                    "**/*Companion*.*",
                    "**/*Kt*.*",
                    "**/com/example/moviedb/di/**/*.*",
                    "**/com/example/moviedb/ui/navigation/**/*.*",
                    "**/com/example/moviedb/ui/widgets/**/*.*"
                )

                classDirectories.setFrom(
                    fileTree("${project.buildDir}/intermediates/classes/${sourcePath}").exclude(
                        excludeFiles
                    ),
                    fileTree("${project.buildDir}/tmp/kotlin-classes/${sourceName}").exclude(
                        excludeFiles
                    )
                )
                val coverageSourceDirs = arrayListOf(
                    "src/main/java",
                    "src/$productFlavorName/java",
                    "src/$buildTypeName/java"
                )

                additionalSourceDirs.setFrom(files(coverageSourceDirs))

                sourceDirectories.setFrom(files(coverageSourceDirs))

                executionData.setFrom(files("${project.buildDir}/jacoco/${testTaskName}.exec"))

                reports {
                    xml.isEnabled = true
                    html.isEnabled = true
                }
                dependsOn(testTaskName)
            }
        }
    }
}

android {

    defaultConfig {
        applicationId = "com.example.moviedb"
        minSdkVersion(17)
        compileSdkVersion(29)
        targetSdkVersion(30)
        multiDexEnabled = true
        vectorDrawables {
            useSupportLibrary = true
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
//            isUseProguard = false
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
//            isUseProguard = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions("default")

    productFlavors {
        create("dev") {
            versionCode = 1
            versionName = "1.0.0"
            applicationIdSuffix = ".dev"
            resValue("string", "app_name", "Movie DB Dev")
            buildConfigField("boolean", "MOCK_DATA", "false")
        }
        create("mock") {
            versionCode = 1
            versionName = "1.0.0"
            applicationIdSuffix = ".mock"
            resValue("string", "app_name", "Movie DB Mock")
            buildConfigField("boolean", "MOCK_DATA", "true")
        }
        create("prd") {
            versionCode = 1
            versionName = "1.0.0"
            resValue("string", "app_name", "Movie DB")
            buildConfigField("boolean", "MOCK_DATA", "false")
        }
    }

    applicationVariants.all {
        buildConfigField("String", "BASE_URL", "\"https://api.themoviedb.org/\"")
        buildConfigField("String", "SMALL_IMAGE_URL", "\"https://image.tmdb.org/t/p/w200\"")
        buildConfigField("String", "LARGE_IMAGE_URL", "\"https://image.tmdb.org/t/p/w500\"")
        buildConfigField("String", "ORIGINAL_IMAGE_URL", "\"https://image.tmdb.org/t/p/original\"")
        buildConfigField("String", "TMBD_API_KEY", "\"d009c99e3df5f6325d18fccd3dfc0a42\"")

        when (name) {
            "dev" -> {
            }
            "prd" -> {
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()

    // https://developer.android.com/topic/libraries/data-binding
    buildFeatures {
        dataBinding = true
    }

    androidExtensions {
        isExperimental = true
    }
}

dependencies {
    // common
    implementation(Libs.appcompat)
    implementation(Libs.legacySupport)
    implementation(Libs.constraintLayout)
    implementation(Libs.recyclerview)
    implementation(Libs.material)
    implementation(Libs.stdLib)
    implementation(Libs.multidex)

    // List of KTX extensions
    // https://developer.android.com/kotlin/ktx/extensions-list
    implementation(Libs.coreKtx)
//    implementation(Libs.activityKtx)
    implementation(Libs.fragmentKtx)
//    implementation(Libs.lifecycleKtx)

    // Lifecycle
    // https://developer.android.com/jetpack/androidx/releases/lifecycle
    implementation(Libs.lifecycleViewModelKtx)
    implementation(Libs.lifecycleLiveDataKtx)
    implementation(Libs.lifecycleJava8)

    // room
    // https://developer.android.com/topic/libraries/architecture/room
    implementation(Libs.roomRuntime)
    kapt(Libs.roomCompiler)
    implementation(Libs.roomKtx)


    implementation(Libs.paging)


    implementation(Libs.navigationRuntimeKtx)
    implementation(Libs.navigationFragmentKtx)
    implementation(Libs.navigationUiKtx)

    implementation(Libs.coroutinesCore)
    implementation(Libs.coroutinesAndroid)
    testImplementation(Libs.coroutinesTest)

    // gson
    implementation(Libs.gson)

    // retrofit
    // https://github.com/square/retrofit
    implementation(Libs.retrofit)
    implementation(Libs.retrofitGson)
    implementation(Libs.okLogging)

    implementation(Libs.glideRuntime)
    kapt(Libs.glideCompiler)


    implementation(Libs.koinViewModel)


    implementation(Libs.easyPermissions)

    implementation(Libs.firebaseAnalytics)
    implementation(Libs.firebaseCrashlytics)

    implementation(Libs.lottie)

    implementation(Libs.timber)

    implementation(Libs.viewpager2)

    // unit test
    testImplementation(Libs.junit)
    testImplementation(Libs.mockitoCore)
    testImplementation(Libs.archCore)
}