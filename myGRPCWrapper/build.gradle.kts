import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.protobuf.gradle.plugin)
    `maven-publish`
}

android {
    namespace = "com.pianisimo.mygrpcwrapper"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Add this to ensure sources are included
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

protobuf {
    protoc {
        artifact = libs.protoc.asProvider().get().toString()
    }
    plugins {
        create("java") {
            artifact = libs.protoc.gen.grpc.java.get().toString()
        }
        create("grpc") {
            artifact = libs.protoc.gen.grpc.java.get().toString()
        }
        create("grpckt") {
            artifact = libs.protoc.gen.grpc.kotlin.get().toString() + ":jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("java") {
                    option("lite")
                }
                create("grpc") {
                    option("lite")
                }
                create("grpckt") {
                    option("lite")
                }
            }
            it.builtins {
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.protoc)
    implementation(libs.protoc.gen.grpc.kotlin)
    implementation(libs.protoc.gen.grpc.java)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf.lite)
    implementation(libs.grpc.okhttp)
    api(libs.protobuf.kotlin.lite)
    api(libs.grpc.kotlin.stub)
}

// Add publishing configuration
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.pianisimo"
            artifactId = "grpc-device-service"
            version = "1.0.1"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("GRPC Device Service Library")
                description.set("Generated gRPC client and server stubs for device service")

                developers {
                    developer {
                        id.set("pianisimo")
                        name.set("Pianisimo")
                    }
                }
            }
        }
    }
}
