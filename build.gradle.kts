plugins {
    `java-library`
    id("io.freefair.lombok") version "9.2.0"
    id("com.diffplug.spotless") version "8.2.1"
    `maven-publish`
    signing
}

group = "com.codechievement.foremka"
// artifactId = "foremka-spring"
version = "1.0.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    // Required for publishing to Maven Central
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-context:7.0.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.2")
    compileOnly("org.slf4j:slf4j-api:2.0.17")

    testImplementation("org.springframework:spring-test:7.0.6")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("com.h2database:h2:2.4.240")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.17")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("CLEANUP_TEST_SCENARIOS", "true")
}

spotless {
    java {
        palantirJavaFormat()
        toggleOffOn()
    }
}

publishing {
    // Using custom local repository to trick Gradle into generating the same artifacts as for Maven Central
    // Default "MavenLocal" setup would include unnecessary "maven-metadata-local.xml" files,
    // and also wouldn't generate required SHA/MD5 checksums
    repositories {
        maven {
            name = "LocalForManualUpload"
            url = project.layout.buildDirectory.dir("m2repo").get().asFile.toURI()
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = "Foremka"
                description = "A framework for managing test data in Spring applications."
                url = "https://github.com/NorbertKawinski/foremka-spring"
                licenses {
                    license {
                        name = "Apache License 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                organization {
                    name = "CodeChievement"
                    url = "https://codechievement.com"
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/NorbertKawinski/foremka-spring/issues"
                }
                developers {
                    developer {
                        id.set("norbertkawinski")
                        name.set("Norbert Kawiński")
                        email.set("norbert@kawinski.net")
                    }
                }
                scm {
                    url = "https://github.com/NorbertKawinski/foremka-spring"
                    connection = "scm:git:git://github.com/NorbertKawinski/foremka-spring.git"
                    developerConnection = "scm:git:ssh://git@github.com:NorbertKawinski/foremka-spring.git"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.register<Zip>("packageForMavenCentral") {
    group = "publishing"
    dependsOn("publishAllPublicationsToLocalForManualUploadRepository")

    val groupPath = project.group.toString().replace('.', '/')
    val artifactId = project.name
    val version = project.version.toString()
    val repoBase = project.layout.buildDirectory.dir("m2repo").get().asFile.toURI()
    val versionDir = repoBase.resolve("${groupPath}/${artifactId}/${version}")

    archiveFileName.set("${artifactId}-${version}-maven-central.zip")
    destinationDirectory.set(layout.buildDirectory.dir("m2repo"))

    from(versionDir) {
        exclude("**/maven-metadata-local.xml")
        into("${groupPath}/${artifactId}/${version}")
    }
}

