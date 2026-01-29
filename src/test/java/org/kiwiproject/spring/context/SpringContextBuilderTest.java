package org.kiwiproject.spring.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

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

    @Nested
    class WithoutShutdownHooks {

        // We cannot reliably assert JVM shutdown hook registration, so instead we:
        // 1. Verify the builder option is applied and context creation succeeds for each context type.
        // 2. Check that the context is functional.
        // 3. Ensure the context can be closed without throwing any exceptions.

        @Test
        void shouldRegisterBeansInParentContext_WhenShutdownHooksDisabled() {
            var bean = new Object();

            ApplicationContext context = new SpringContextBuilder()
                    .withoutShutdownHooks()
                    .addParentContextBean("myBean", bean)
                    .addAnnotationConfiguration(SampleTestConfiguration.class)
                    .build();

            assertThat(context.getParent()).isNotNull();
            assertThat(context.getParent().containsBean("myBean")).isTrue();
            assertThat(context.getParent().getBean("myBean")).isSameAs(bean);

            assertThat(context.getBean("myBean"))
                    .describedAs("myBean should also be visible from child context")
                    .isSameAs(bean);

            assertCanClose(context);
        }

        @Test
        void shouldDisableShutdownHooks_ForAnnotationContext() {
            ApplicationContext context = builder
                    .withoutShutdownHooks()
                    .addAnnotationConfiguration(SampleTestConfiguration.class)
                    .build();

            assertThat(context).isNotNull();
            assertThat(context.getParent()).isNotNull();

            assertThat(context.getBean(OtherTestBean.class)).isNotNull();

            assertCanClose(context);
        }

        @Test
        void shouldDisableShutdownHooks_ForXmlContext() {
            ApplicationContext context = builder
                    .withoutShutdownHooks()
                    .addXmlConfigLocation("SpringContextBuilderTest/testApplicationContext.xml")
                    .build();

            assertThat(context).isNotNull();
            assertThat(context.getParent()).isNotNull();

            assertThat(context.getBean(OtherTestBean.class)).isNotNull();

            assertCanClose(context);
        }

        private static void assertCanClose(ApplicationContext context) {
            assertThat(context)
                    .describedAs("The context should be a ConfigurableApplicationContext")
                    .isInstanceOf(ConfigurableApplicationContext.class);

            var configurableContext = (ConfigurableApplicationContext) context;

            assertThatCode(configurableContext::close)
                    .describedAs("Closing the context should not throw any exceptions")
                    .doesNotThrowAnyException();
        }
    }
}
