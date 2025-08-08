plugins {
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.5"
    id("java")
}
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    
    // Testing dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("com.h2database:h2")
    testImplementation("io.r2dbc:r2dbc-h2")
}
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs = listOf("-Dreactor.netty.http.server.accessLogEnabled=true")
}