# Module Development (incomplete)

In order to develop a new module, you'll need to do the following steps:

1. Create a new folder for your module. I recommend using the convention of "{protocolName}-module", IE for a Facebook
   module, call your folder "facebook-module".
2. Modify `settings.gradle.kts` to add a new `include("{folderName}")`. IE, for your Facebook module, add
   `include("facebook-module")`. This tells Gradle that your folder is a subproject.
3. Create a `build.gradle.kts` file in your sub folder. Fill it with the following template:

```kotlin
group = "my.package" // I believe this is optional

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "2.0.0"
}

dependencies {
    implementation(project(":")) // This includes the root project as a dependency, which is required to implement the interfaces

    implementation(libs.guice) // The main interface requires Guice
}

// I think this is optional, but probably good to include and not rely on convention
sourceSets {
    main {
        kotlin { srcDir("src/main/kotlin") }
        resources { srcDir("src/main/resources") }
    }

    test {
        kotlin { srcDir("src/test/kotlin") }
        resources { srcDir("src/test/resources") }
    }
}

// I don't know why this is needed, will investigate. The build fails if you don't have it, though.
tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE // TODO review this
    }
}

kotlin {
    explicitApi() // This is optional. You can also use explicitApiWarning(), or nothing. This setting will make it an error to not explicitly specify the visibility of your classes, methods, etc.
}

```

4. Create a class implementing the `au.com.skater901.wc3connect.NotificationModule` interface. (Standard practice is to
   create your classes under `src/main/kotlin/your/package`)
5. Create a configuration class. This must be a class with a primary constructor.
6. Create a file called `au.com.skater901.wc3connect.NotificationModule` in `src/main/resources/META-INF/services`.
   Inside this file, put the fully qualified name of your class that implements `NotificationModule`. This file is the
   special sauce that makes your module available to the main app. It's using Java'
   s [Service Provider Interface](https://www.baeldung.com/java-spi) mechanism to find and load the class at runtime.
7. Modify `build.gradle.kts` in the root directory. In the `dependencies` section, find the section titled
   `// Notification Modules`. In that section, add `runtimeOnly(project(":moduleName"))` This will include your module
   in the runtime classpath of the main application.

## Tips

In order to keep versions consistent across the whole project, if you have a dependency you require that is already used
by the main project or by another module, you should instead declare that dependency
in [gradle/libs.versions.toml](gradle/libs.versions.toml). This is a central location for declaring dependencies that
makes them available to all projects. Remove the dependency from the other modules/main project, so that it's only
specified in libs.versions.toml.

## Known Issues

Currently, all notifications will be saved as Discord notifications. I'm working on a solution for this.

Running the main app will NOT compile your subproject. You have to tell Gradle to compile your subproject if you want it
to be used by the main app. You can do this by navigating into the directory and running `../gradlew build`.