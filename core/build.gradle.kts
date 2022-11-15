plugins {
    id("java")
    id("io.freefair.lombok") version "6.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.porama"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("org.jetbrains:annotations:20.1.0")
    implementation("org.projectlombok:lombok:1.18.22")
    implementation("com.google.code.gson:gson:2.9.1")

    implementation("com.rabbitmq:amqp-client:5.16.0")
    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("ch.qos.logback:logback-classic:1.4.3")

    implementation(project(":common"))

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes.set("Main-Class", "dev.porama.gradingcore.core.Main");
    }
}

tasks.build.get().dependsOn(tasks.shadowJar.get());