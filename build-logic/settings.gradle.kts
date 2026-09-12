dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://chaquo.com/maven")
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"