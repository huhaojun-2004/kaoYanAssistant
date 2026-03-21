pluginManagement {
    repositories {
        // Keep multiple mirrors ahead of Maven Central to reduce TLS issues on mainland networks.
        google()
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://mirrors.tencent.com/nexus/repository/maven-public/")
        maven(url = "https://maven.aliyun.com/repository/central")
        maven(url = "https://maven.aliyun.com/repository/public")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Required so Gradle/Android Studio can resolve JDK downloads for updateDaemonJvm.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Prefer reachable mirrors first, then fall back to upstream repositories.
        google()
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://mirrors.tencent.com/nexus/repository/maven-public/")
        maven(url = "https://maven.aliyun.com/repository/central")
        maven(url = "https://maven.aliyun.com/repository/public")
        mavenCentral()
    }
}

rootProject.name = "kaoYanAssistant"
include(":app")
 
