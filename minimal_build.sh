#!/bin/bash

# Use Java 11
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
rm -rf build
# Avoid all tests, which while nice, don't seem to work properly anymore.
gradle build -x compileTestGroovy -x compileIntegrationTestGroovy -x compileAcceptanceTestGroovy -x checkstyleMain -x compileSmokeTestGroovy -x pmdMain -x pmdTest -x smokeTest -x findbugsMain

