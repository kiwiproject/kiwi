### Kiwi
[![Build](https://github.com/kiwiproject/kiwi/workflows/build/badge.svg)](https://github.com/kiwiproject/kiwi/actions?query=workflow%3Abuild)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kiwiproject_kiwi&metric=alert_status)](https://sonarcloud.io/dashboard?id=kiwiproject_kiwi)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kiwiproject_kiwi&metric=coverage)](https://sonarcloud.io/dashboard?id=kiwiproject_kiwi)
[![CodeQL](https://github.com/kiwiproject/kiwi/actions/workflows/codeql.yml/badge.svg)](https://github.com/kiwiproject/kiwi/actions/workflows/codeql.yml)
[![javadoc](https://javadoc.io/badge2/org.kiwiproject/kiwi/javadoc.svg)](https://javadoc.io/doc/org.kiwiproject/kiwi)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/org.kiwiproject/kiwi)](https://central.sonatype.com/artifact/org.kiwiproject/kiwi/2.5.0)

Kiwi is a utility library. It contains a variety of utilities that we have built over time and find useful.
In general, we look first to either Google Guava or Apache Commons for utilities, but if they don't have something
we need, or if what they have isn't exactly what we want, then we'll (probably) add it here.

Almost all the dependencies in the POM have _provided_ scope, so that we don't bring in a ton of required dependencies.
This downside to this is that you must specifically add any required dependencies to your own POM in order to use a
specific feature in Kiwi.

The only required dependencies are guava, commons-lang3, and slf4j-api. If you use the Maven Enforcer plugin, you could therefore
run into dependency convergence errors if the kiwi versions are different from the ones you're using.
