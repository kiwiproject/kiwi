package org.kiwiproject.spring.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SecondTestConfiguration {

    @Bean
    public SampleTestBean sampleTestBean3() {
        return new SampleTestBean("test bean 3", 256);
    }
}
