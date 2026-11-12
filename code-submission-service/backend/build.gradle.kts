plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ilep"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

// Tests that start real Docker sandbox containers are tagged "docker".
// Run everything with: ./gradlew test
// Skip the sandbox tests with: ./gradlew test -PexcludeDockerTests
tasks.test {
    useJUnitPlatform {
        if (project.hasProperty("excludeDockerTests")) {
            excludeTags("docker")
        }
    }
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// The student editor (../frontend) is built with npm and served by this service as static files.
val frontendDirectory = layout.projectDirectory.dir("../frontend")
val generatedStaticResources = layout.buildDirectory.dir("generated-resources/frontend")

val buildFrontend by tasks.registering(Exec::class) {
    description = "Builds the React student editor with npm."
    workingDir = frontendDirectory.asFile
    commandLine("sh", "-c", "npm ci && npm run build")
    inputs.dir(frontendDirectory.dir("src"))
    inputs.files(frontendDirectory.file("package.json"), frontendDirectory.file("package-lock.json"), frontendDirectory.file("index.html"))
    outputs.dir(frontendDirectory.dir("dist"))
}

val copyFrontendIntoStaticResources by tasks.registering(Copy::class) {
    description = "Copies the built student editor into the service's static resources."
    dependsOn(buildFrontend)
    from(frontendDirectory.dir("dist"))
    into(generatedStaticResources.map { it.dir("static") })
}

sourceSets {
    main {
        resources.srcDir(generatedStaticResources)
    }
}

// Only the runnable jar bundles the editor; tests and bootRun do not need npm.
tasks.named("bootJar") {
    dependsOn(copyFrontendIntoStaticResources)
}
tasks.named<ProcessResources>("processResources") {
    mustRunAfter(copyFrontendIntoStaticResources)
}
