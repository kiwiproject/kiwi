package org.kiwiproject.spring.context;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:SpringContextBuilderTest/testApplicationContext.xml")
class XmlImportingTestConfiguration {
}
