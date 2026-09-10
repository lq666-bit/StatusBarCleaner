pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
        // libxposed-api 只在 JitPack 上有
        maven("https://jitpack.io")
    }
}

rootProject.name = "StatusBarCleaner"
include(":app")