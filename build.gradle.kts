import com.diffplug.spotless.kotlin.KtfmtStep

val quarkusPlatformVersion = providers.gradleProperty("quarkusPlatformVersion").get()

plugins {
    kotlin("jvm") version "2.4.20"
    id("io.quarkus") version "4.0.0.CR1"
    id("com.diffplug.spotless") version "8.10.2"
}

group = "io.github.nanamochi"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("tools.jackson.module:jackson-module-kotlin")

    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-elytron-security-common")
    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-jdbc-mariadb")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-redis-client")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")

    implementation("org.flywaydb:flyway-mysql")

    implementation("io.github.7mochi:chio:0.0.1")

    testImplementation("io.quarkus:quarkus-junit")
    testImplementation("io.quarkus:quarkus-junit-mockito")
    testImplementation("io.quarkus:quarkus-jdbc-h2")
    testImplementation("io.rest-assured:rest-assured")
}

spotless {
    format("misc") {
        target(".gitignore", ".gitattributes", "*.md")
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlin {
        target("src/**/*.kt")
        targetExclude(".direnv/**")
        ktfmt("0.61").kotlinlangStyle().configure {
            it.setMaxWidth(100)
            it.setRemoveUnusedImports(true)
            it.setTrailingCommaManagementStrategy(KtfmtStep.TrailingCommaManagementStrategy.COMPLETE)
        }
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
}

tasks.test {
    useJUnitPlatform()
}
