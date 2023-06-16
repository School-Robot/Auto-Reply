plugins {
    val kotlinVersion = "1.8.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.15.0-M1"
}

group = "tk.mcsog"
version = "0.1.2"


repositories {
    mavenCentral()
}
