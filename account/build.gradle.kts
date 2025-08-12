plugins {
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.5"
    id("java")
}
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    
    // JPA for database access
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.h2database:h2")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Testing dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")

    
    // Lombok for tests
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs = listOf("-Dreactor.netty.http.server.accessLogEnabled=true")
}

tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}