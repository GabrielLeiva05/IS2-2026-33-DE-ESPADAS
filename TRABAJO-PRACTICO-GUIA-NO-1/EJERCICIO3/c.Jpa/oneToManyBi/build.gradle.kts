plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-envers
    implementation(group = "org.hibernate.orm", name = "hibernate-envers", version = "6.6.13.Final")
// https://mvnrepository.com/artifact/com.h2database/h2
    implementation(group = "com.h2database", name = "h2", version = "2.3.232")
}

tasks.test {
    useJUnitPlatform()
}