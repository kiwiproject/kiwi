package org.kiwiproject.spring.context;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("SpringContextBuilderTest/testApplicationContext.xml")
public class XmlImportingTestConfiguration {
}
