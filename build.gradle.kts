plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
  id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer and runMojangMappedServer tasks for testing
}

group = "me.zombie_striker.PsudoCommands"
version = "2.0"
description = "TODO"

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}


dependencies {
  paperweight.paperDevBundle("1.21.7-R0.1-SNAPSHOT")
}

tasks {
  compileJava {
    // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }

  jar {

  }
}
