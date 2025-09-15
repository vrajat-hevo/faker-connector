import java.net.URL
import java.util.Base64

plugins {
    `java`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.diffplug.spotless") version "6.20.0"
    id("org.sonarqube") version "5.0.0.4638"
    id("maven-publish")
    jacoco
}

fun downloadCredentials(): Map<String, String> {
    val isCI = System.getenv("CIRCLECI") == "true"
    val url: URL = if (isCI) {
        val urlString = System.getenv("HEVO_M2_WAGON_AUTH_URL")
        URL(urlString)
    } else {
        URL("https://aws-key.hevo.me/mvn-read")
    }

    val awsResponseStr = if (isCI) {
        val urlConnection = url.openConnection()
        val userCredentials = "circleci:" + System.getenv("HEVO_M2_WAGON_PASSWORD")
        val basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.toByteArray())
        urlConnection.setRequestProperty("Authorization", basicAuth)
        urlConnection.inputStream.bufferedReader().use { it.readText() }
    } else {
        url.readText()
    }

    val jsonSlurper = groovy.json.JsonSlurper() // Ensure JsonSlurper is accessible
    return jsonSlurper.parseText(awsResponseStr) as Map<String, String>
}

val awsResponseJson = downloadCredentials()

val baseMavenURL = "s3://hevo-artifacts/package/mvn"
val releaseRepoURL = "$baseMavenURL/release"
val snapshotRepoURL = "$baseMavenURL/snapshot"

// Repositories configuration at the root level
repositories {
    maven {
        name = "hevoMavenRelease"
        url = uri("$releaseRepoURL")
        credentials(AwsCredentials::class) {
            accessKey = awsResponseJson["access_key"].orEmpty()
            secretKey = awsResponseJson["secret_key"].orEmpty()
            sessionToken = awsResponseJson["token"].orEmpty()
        }
    }

    maven {
        name = "hevoMavenSnapshot"
        url = uri("$snapshotRepoURL")
        credentials(AwsCredentials::class) {
            accessKey = awsResponseJson["access_key"].orEmpty()
            secretKey = awsResponseJson["secret_key"].orEmpty()
            sessionToken = awsResponseJson["token"].orEmpty()
        }
    }

    mavenCentral() // Add Maven Central for other dependencies
    google()
}

dependencies {
    implementation("io.hevo:hevo-sdk:1.9.0-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-junit-jupiter:5.19.0")
    testImplementation("org.mockito:mockito-core:5.19.0")
}

// Spotless plugin configuration at the root level
spotless {
    java {
        target("**/*.java")
        googleJavaFormat("1.18.1")
        removeUnusedImports()
    }

    kotlinGradle {
        // Targets the root build.gradle.kts and all subprojects' build.gradle.kts files
        target("**/*.gradle.kts")

        ktlint("0.49.1") // Use a specific version of ktlint
        trimTrailingWhitespace() // Remove trailing whitespaces
        indentWithSpaces(4) // Use 4 spaces for indentation
        endWithNewline() // Ensure the file ends with a newline
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.hevo.connector.application.Main"
    }

    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))

    // Exclude specific files from being included in the JAR's META-INF
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.shadowJar {
    // Exclude default configurations
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    // Specify the main class for the JAR
    manifest {
        attributes["Main-Class"] = "io.hevo.connector.application.Main"
    }
    // Merging Service Files
    mergeServiceFiles()
}

defaultTasks("jar", "shadowJar")
application {
    // Define the main class for the application
    mainClass.set("io.hevo.connector.application.Main")
}