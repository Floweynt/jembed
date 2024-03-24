plugins {
    id("java")
}

group = "com.floweytf.jembed"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.0")
    implementation(project("jvmir"))
}
