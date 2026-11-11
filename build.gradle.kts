plugins {
    id("java")
    application
}

application {
    mainClass = "com.back.Main"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

group = "com.back"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// 채점 환경이 어떤 JDK 든 Java 21 기준으로 컴파일한다. (List.reversed() 등 21 API 사용)
tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    runtimeOnly("com.mysql:mysql-connector-j:9.5.0")
    // Source: https://mvnrepository.com/artifact/org.assertj/assertj-core
    testImplementation("org.assertj:assertj-core:3.27.7")

    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    testCompileOnly("org.projectlombok:lombok:1.18.48")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.48")
}

tasks.test {
    useJUnitPlatform()
}