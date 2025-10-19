plugins {
    java
    `java-library`
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
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnlyApi("com.thewinterframework:wintercore:1.0.0")
    //compileOnly(files("libs/wintercore-1.0.0.jar"))
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.14")

    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("org.xerial:sqlite-jdbc:3.46.0.0")
    compileOnly("com.mysql:mysql-connector-j:8.4.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.17.0")
}
