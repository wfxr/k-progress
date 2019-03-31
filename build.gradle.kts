group = "com.github.wfxr"
version = "0.1.0"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.21")
    id("com.github.johnrengelman.shadow").version("5.0.0")
    maven
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation("com.googlecode.lanterna:lanterna:3.0.1")
    implementation("org.apache.commons:commons-lang3:3.8.1")
}

tasks {
    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    artifacts {
        add("archives", sourcesJar)
    }
}
