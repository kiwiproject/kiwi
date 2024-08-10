### Kiwi
[![Build](https://github.com/kiwiproject/kiwi/workflows/build/badge.svg)](https://github.com/kiwiproject/kiwi/actions?query=workflow%3Abuild)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kiwiproject_kiwi&metric=alert_status)](https://sonarcloud.io/dashboard?id=kiwiproject_kiwi)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kiwiproject_kiwi&metric=coverage)](https://sonarcloud.io/dashboard?id=kiwiproject_kiwi)
[![CodeQL](https://github.com/kiwiproject/kiwi/actions/workflows/codeql.yml/badge.svg)](https://github.com/kiwiproject/kiwi/actions/workflows/codeql.yml)
[![javadoc](https://javadoc.io/badge2/org.kiwiproject/kiwi/javadoc.svg)](https://javadoc.io/doc/org.kiwiproject/kiwi)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/org.kiwiproject/kiwi)](https://central.sonatype.com/artifact/org.kiwiproject/kiwi/)

Kiwi is a utility library. It contains a variety of utilities that we have built over time and find useful.
In general, we look first to either Google Guava or Apache Commons for utilities, but if they don't have something
we need, or if what they have isn't exactly what we want, then we'll (probably) add it here.

Almost all the dependencies in the POM have _provided_ scope, so that we don't bring in a ton of required dependencies.
This downside to this is that you must specifically add any required dependencies to your own POM in order to use a
specific feature in Kiwi.

The only required dependencies are guava, commons-lang3, and slf4j-api. If you use the Maven Enforcer plugin, you could therefore
run into dependency convergence errors if the kiwi versions are different from the ones you're using.

#### Validation Annotations

As of kiwi 3.4.0, the validation annotations in the `org.kiwiproject.kiwi.validation` package use Java's
[ServiceLoader](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html) mechanism.
The constraint implementations are defined in `META-INF/services/jakarta.validation.ConstraintValidator` and
the validation message bundle is located in `ContributorValidationMessages.properties`. This allows kiwi to
provide its custom constraints without interfering with an application that defines its own constraints and
message bundle in its own `ValidationMessages.properties`.

The [Hibernate Validator](https://hibernate.org/validator/) reference guide describes this in
[Constraint definitions via ServiceLoader](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#_constraint_definitions_via_serviceloader).
Another good resource is [Adding custom constraint definitions via the Java service loader](https://in.relation.to/2017/03/02/adding-custom-constraint-definitions-via-the-java-service-loader/).

If you are using kiwi's custom constraints _in addition to custom constraints provided by another library_, then
this requires some additional configuration, otherwise only one of the `ContributorValidationMessages.properties`
provided by each library will be found, and therefore the custom messages for some constraints won't be found
during validation. To fix this, all `ContributorValidationMessages.properties` files must be combined into a
single file, for example, using the [Maven Shade plugin](https://maven.apache.org/plugins/maven-shade-plugin/) and an
[AppendingTransformer](https://maven.apache.org/plugins/maven-shade-plugin/examples/resource-transformers.html#AppendingTransformer):

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.5.3</version>
  <executions>
    <execution>
      <goals>
        <goal>shade</goal>
      </goals>
      <configuration>
        <transformers>
          <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
            <resource>ContributorValidationMessages.properties</resource>
          </transformer>
        </transformers>
      </configuration>
    </execution>
  </executions>
</plugin>
```

With this additional build step, multiple libraries can each provide custom constraints.
