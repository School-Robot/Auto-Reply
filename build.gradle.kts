plugins {
    val kotlinVersion = "1.8.22"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.15.0"
}

group = "tk.mcsog"
version = "0.2.1"

repositories {
    mavenCentral()
}
