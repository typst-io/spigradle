plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(property("java.version")!!.toString().toInt()))
    }
}
