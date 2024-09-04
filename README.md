# WC3 Notification Bot

This application can be used to poll for Warcraft 3 games hosted on WC3Connect, Battle.Net, and potentially other
systems, and send notifications about the games.
This application supports modules which can be developed for different systems to allow notifications to be sent to
different systems. Currently supported systems are:

- [Discord](discord-module)

## Running this application

To run this application, you'll need to do the following steps:

1. Create a MariaDB database, with a `wc3_bot` schema.
2. Create a user with full access to the `wc3_bot` schema.
3. Create a Java properties file with configuration values. Configuration will be discussed below.
4. Run the application using the following command: `./gradlew run -PconfigFile=path/to/properties/file.properties`

## Configuration

Configuration for the main application consists of four parts; application configuration, database configuration,
games provider configuration, and logging configuration.

Additionally, all used modules will require configuration to be specified as well. Read the documentation for the
modules to see what configuration they require.

### Application Configuration

Application configuration consists of one property:

- `application.refreshInterval` is how often (in milliseconds) the various game sources will be polled to retrieve the
  list of hosted games. This value defaults to `10_000` if not specified in the config Properties file.

### Database Configuration

Database configuration consists of four properties:

- `database.host` is the address of the database. This value defaults to `localhost` if not specified in the config
  Properties file.
- `database.port` is the port of the database. This value defaults to `3306` if not specified in the config Properties
  file.
- `database.username` is the username of the MariaDB user this application will use to connect to the database.
- `database.password` is the password of the MariaDB user this application will use to connect to the database.

### Games Provider Configuration

Games Provider configuration currently consists of two values. However, more values will be added here if and when more
game providers are added.

These configuration properties all have default values, and most likely will never need to be set. It's highly unlikely
that the game providers will ever change their URLs.

- `wc3connect.url` is the URL of the list of games hosted on WC3Connect. This value defaults to
  `https://host.entgaming.net/allgames` if not set.
- `wc3stats.url` is the URL of the list of games hosted on Battle.Net, provided by WC3Stats' API. This value defaults to
  `https://api.wc3stats.com/gamelist` if not set.

### Logging Configuration

Logging configuration consists of four properties:

- `logging.consoleLoggingLevel` is the threshold of logs that will be logged to the console. This value defaults to
  `OFF` if not specified in the config Properties file.
- `logging.fileLoggingLevel` is the threshold of logs that will be logged to file. This value defaults to `INFO` if not
  specified in the config Properties file.
- `logging.logFileDirectory` is directory where the log file will be created. This value defaults to `/local/logs` if
  not specified in the config Properties file.
- `logging.logFileArchiveCount` is the number of days worth of file logs to keep. This value defaults to `7` if not
  specified in the config Properties file.

Valid logging levels can be
seen [here](https://github.com/qos-ch/logback/blob/master/logback-classic/src/main/java/ch/qos/logback/classic/Level.java#L47-L84)

## Module Development

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
    alias(libs.plugins.kotlin)
}

dependencies {
    implementation(project(":module-api"))

    implementation(project(":utilities")) // Optional dependency with various utility functions

    implementation(libs.coroutines) // Optional if you want coroutines

    implementation(libs.guice)
}

kotlin {
    explicitApi() // This is optional. You can also use explicitApiWarning(), or nothing. This setting will make it an error to not explicitly specify the visibility of your classes, methods, etc.
}

```

4. Create a class implementing the `au.com.skater901.wc3.api.NotificationModule` interface. (Standard practice is
   to create your classes under `src/main/kotlin/your/package`)
5. Create a configuration class. This must be a class with a primary constructor.
6. Create a file called `au.com.skater901.wc3.api.NotificationModule` in `src/main/resources/META-INF/services`.
   Inside this file, put the fully qualified name of your class that implements `NotificationModule`. This file is the
   special sauce that makes your module available to the main app. It's using Java's
   [Service Provider Interface](https://www.baeldung.com/java-spi) mechanism to find and load the class at runtime.
7. Modify `build.gradle.kts` in the root directory. In the `dependencies` section, find the section titled
   `// Notification Modules`. In that section, add `implementation(project(":moduleName"))` This will include your
   module in the runtime classpath of the main application.

### Tips

In order to keep versions consistent across the whole project, if you have a dependency you require that is already used
by the main project or by another module, you should instead declare that dependency
in [gradle/libs.versions.toml](gradle/libs.versions.toml). This is a central location for declaring dependencies that
makes them available to all projects. Remove the dependency from the other modules/main project, so that it's only
specified in libs.versions.toml.