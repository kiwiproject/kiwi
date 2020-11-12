package org.kiwiproject.spring.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleTestConfiguration {

    @Bean
    public SampleTestBean sampleTestBean1() {
        return new SampleTestBean("test bean 1", 126);
    }

    @Bean
    public SampleTestBean sampleTestBean2() {
        return new SampleTestBean("test bean 2", 126);
    }

    @Bean
    public OtherTestBean otherTestBean() {
        return new OtherTestBean("other bean 1", 2048, sampleTestBean1());
    }
}
