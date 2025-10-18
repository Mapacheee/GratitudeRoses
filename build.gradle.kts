plugins {
    java
    `java-library`
    id("com.gradleup.shadow") version "8.3.2"
    id("io.github.revxrsal.zapper") version "1.0.3"
}

group = "me.mapacheee.gratituderoses"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    compileOnlyApi("com.thewinterframework:wintercore:1.0.0")
    //api("com.thewinterframework:paper:1.0.4")
    //annotationProcessor("com.thewinterframework:paper:1.0.4")

    //api("com.thewinterframework:configuration:1.0.1")
    //annotationProcessor("com.thewinterframework:configuration:1.0.1")

    //api("com.thewinterframework:command:1.0.1")
    //annotationProcessor("com.thewinterframework:command:1.0.1")

    zap("com.zaxxer:HikariCP:5.1.0")
    zap("org.xerial:sqlite-jdbc:3.46.0.0")
    zap("com.mysql:mysql-connector-j:8.4.0")

    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")
    implementation("io.github.revxrsal:zapper.api:1.0.3")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.14")
}

zapper {
    libsFolder = "libraries"
    relocationPrefix = "me.mapacheee.gratituderoses.libs"
    relocate("com.zaxxer.hikari", "hikari")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description
        )
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
