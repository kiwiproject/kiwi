package org.kiwiproject.spring.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.Map;

@DisplayName("SpringContextBuilder")
class SpringContextBuilderTest {

    private SpringContextBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new SpringContextBuilder();
    }

    @Nested
    class AddParentContextBean {

        @Test
        void shouldRegisterParentContextBeans() {
            var sampleTestBean1 = new SampleTestBean("test bean 1", 42);
            var sampleTestBean2 = new SampleTestBean("test bean 2", 84);

            ApplicationContext context = builder
                    .addParentContextBean("sampleTestBean1", sampleTestBean1)
                    .addParentContextBean("sampleTestBean2", sampleTestBean2)
                    .build();

            assertThat(context.getParent()).isNotNull();
            Map<String, SampleTestBean> sampleBeans = context.getParent().getBeansOfType(SampleTestBean.class);
            assertThat(sampleBeans).hasSize(2);
            assertThat(context.getBean("sampleTestBean1", SampleTestBean.class)).isSameAs(sampleTestBean1);
            assertThat(context.getBean("sampleTestBean2", SampleTestBean.class)).isSameAs(sampleTestBean2);
        }
    }

    @Nested
    class AddAnnotationConfiguration {

        @Test
        void shouldAddOneAnnotationConfiguration() {
            ApplicationContext context = builder
                    .addAnnotationConfiguration(SampleTestConfiguration.class)
                    .build();

            var sampleTestBean1 = context.getBean("sampleTestBean1", SampleTestBean.class);
            assertThat(sampleTestBean1).isNotNull();
            assertThat(sampleTestBean1.getName()).isEqualTo("test bean 1");
            assertThat(sampleTestBean1.getValue()).isEqualTo(126);

            assertThat(context.getBean("sampleTestBean2")).isNotNull();

            var otherTestBean = context.getBean(OtherTestBean.class);
            assertThat(otherTestBean).isNotNull();
            assertThat(otherTestBean.getName()).isEqualTo("other bean 1");
            assertThat(otherTestBean.getValue()).isEqualTo(2048);
            assertThat(otherTestBean.getSampleTestBean()).isSameAs(sampleTestBean1);
        }

        @Test
        void shouldThrowIllegalStateException_WhenXmlConfigLocationsHaveBeenSpecified() {
            assertThatIllegalStateException()
                    .isThrownBy(() ->
                            builder.addXmlConfigLocation("testApplicationContext.xml")
                                    .addAnnotationConfiguration(SampleTestConfiguration.class)
                                    .build())
                    .withMessageStartingWith("XML config locations have already been specified");
        }

        @Test
        void shouldSupportUsingAnnotatedClasses_CombinedWith_ImportedXmlConfiguration() {
            ApplicationContext context = builder
                    .addAnnotationConfiguration(XmlImportingTestConfiguration.class)
                    .addAnnotationConfiguration(SecondTestConfiguration.class)
                    .build();

            assertThat(context.getBean("sampleTestBean1", SampleTestBean.class).getName()).isEqualTo("test bean 1");
            assertThat(context.getBean("sampleTestBean2", SampleTestBean.class).getName()).isEqualTo("test bean 2");
            assertThat(context.getBean("sampleTestBean3", SampleTestBean.class).getName()).isEqualTo("test bean 3");
        }
    }

    @Nested
    class WithAnnotationConfigurations {

        @Test
        void shouldAddMultipleAnnotationConfigurations() {
            ApplicationContext context = builder
                    .withAnnotationConfigurations(SampleTestConfiguration.class, SecondTestConfiguration.class)
                    .build();

            assertThat(context.getBean("sampleTestBean1", SampleTestBean.class).getName()).isEqualTo("test bean 1");
            assertThat(context.getBean("sampleTestBean2", SampleTestBean.class).getName()).isEqualTo("test bean 2");
            assertThat(context.getBean("sampleTestBean3", SampleTestBean.class).getName()).isEqualTo("test bean 3");
        }

        @Test
        void shouldThrowIllegalStateException_WhenXmlConfigLocationsHaveBeenSpecified() {
            assertThatIllegalStateException()
                    .isThrownBy(() ->
                            builder.addXmlConfigLocation("SpringContextBuilderTest/testApplicationContext.xml")
                                    .withAnnotationConfigurations(SampleTestConfiguration.class)
                                    .build())
                    .withMessageStartingWith("XML config locations have already been specified");
        }
    }

    @Nested
    class AddXmlConfigLocation {

        @Test
        void shouldAddOneXmlConfigLocation() {
            ApplicationContext context = builder
                    .addXmlConfigLocation("SpringContextBuilderTest/testApplicationContext.xml")
                    .build();

            var sampleTestBean1 = context.getBean("sampleTestBean1", SampleTestBean.class);
            assertThat(sampleTestBean1).isNotNull();
            assertThat(sampleTestBean1.getName()).isEqualTo("test bean 1");
            assertThat(sampleTestBean1.getValue()).isEqualTo(42);

            assertThat(context.getBean("sampleTestBean2")).isNotNull();

            var otherTestBean = context.getBean(OtherTestBean.class);
            assertThat(otherTestBean).isNotNull();
            assertThat(otherTestBean.getName()).isEqualTo("other bean 1");
            assertThat(otherTestBean.getValue()).isEqualTo(1024);
            assertThat(otherTestBean.getSampleTestBean()).isSameAs(sampleTestBean1);
        }

        @Test
        void shouldThrowIllegalStateException_WhenAnnotationConfigurationHasBeenSpecified() {
            assertThatIllegalStateException()
                    .isThrownBy(() ->
                            builder.addAnnotationConfiguration(SampleTestConfiguration.class)
                                    .addXmlConfigLocation("testApplicationContext.xml")
                                    .build())
                    .withMessageStartingWith("Annotated classes have already been specified");
        }
    }

    @Nested
    class WithXmlConfigLocations {

        @Test
        void shouldAddMultipleXmlConfigLocations() {
            ApplicationContext context = builder
                    .withXmlConfigLocations(
                            "SpringContextBuilderTest/testApplicationContext.xml",
                            "SpringContextBuilderTest/secondTestApplicationContext.xml")
                    .build();

            assertThat(context.getBean("sampleTestBean1", SampleTestBean.class).getName()).isEqualTo("test bean 1");
            assertThat(context.getBean("sampleTestBean2", SampleTestBean.class).getName()).isEqualTo("test bean 2");
            assertThat(context.getBean("sampleTestBean3", SampleTestBean.class).getName()).isEqualTo("test bean 3");
        }

        @Test
        void shouldThrowIllegalStateException_WhenAnnotationConfigurationHasBeenSpecified() {
            assertThatIllegalStateException()
                    .isThrownBy(() ->
                            builder.addAnnotationConfiguration(SampleTestConfiguration.class)
                                    .withXmlConfigLocations("SpringContextBuilderTest/testApplicationContext.xml",
                                            "SpringContextBuilderTest/secondTestApplicationContext.xml")
                                    .build())
                    .withMessageStartingWith("Annotated classes have already been specified");
        }
    }
}
