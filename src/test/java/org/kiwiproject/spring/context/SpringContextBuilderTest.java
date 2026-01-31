package org.kiwiproject.spring.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
    class Contexts {

        @SuppressWarnings("deprecation")
        @Test
        void shouldBeCreatedUsingBuild() {
            var context = builder
                    .addAnnotationConfiguration(SampleTestConfiguration.class)
                    .build();

            assertThat(context)
                    .describedAs("expected actual type to be ConfigurableApplicationContext")
                    .isInstanceOf(ConfigurableApplicationContext.class);

            assertThat(context.getBeanDefinitionNames()).contains(
                    "sampleTestBean1", "sampleTestBean2", "otherTestBean");

            assertCanClose((ConfigurableApplicationContext) context);
        }

        @Test
        void shouldBeCreatedUsingBuildConfigurableContext() {
            var context = builder
                    .addAnnotationConfiguration(SampleTestConfiguration.class)
                    .buildConfigurableContext();

            assertThat(context.getBeanDefinitionNames()).contains(
                    "sampleTestBean1", "sampleTestBean2", "otherTestBean");

            assertCanClose(context);
        }
    }

    @Nested
    class AddParentContextBean {

        @Test
        void shouldRegisterParentContextBeans() {
            var sampleTestBean1 = new SampleTestBean("test bean 1", 42);
            var sampleTestBean2 = new SampleTestBean("test bean 2", 84);

            var context = builder
                    .addParentContextBean("sampleTestBean1", sampleTestBean1)
                    .addParentContextBean("sampleTestBean2", sampleTestBean2)
                    .buildConfigurableContext();

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
            var context = builder
                    .addAnnotationConfiguration(SampleTestConfiguration.class)
                    .buildConfigurableContext();

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
                                    .buildConfigurableContext())
                    .withMessageStartingWith("XML config locations have already been specified");
        }

        @Test
        void shouldSupportUsingAnnotatedClasses_CombinedWith_ImportedXmlConfiguration() {
            var context = builder
                    .addAnnotationConfiguration(XmlImportingTestConfiguration.class)
                    .addAnnotationConfiguration(SecondTestConfiguration.class)
                    .buildConfigurableContext();

            assertThat(context.getBean("sampleTestBean1", SampleTestBean.class).getName()).isEqualTo("test bean 1");
            assertThat(context.getBean("sampleTestBean2", SampleTestBean.class).getName()).isEqualTo("test bean 2");
            assertThat(context.getBean("sampleTestBean3", SampleTestBean.class).getName()).isEqualTo("test bean 3");
        }
    }

    @Nested
    class WithAnnotationConfigurations {

        @Test
        void shouldAddMultipleAnnotationConfigurations() {
            var context = builder
                    .withAnnotationConfigurations(SampleTestConfiguration.class, SecondTestConfiguration.class)
                    .buildConfigurableContext();

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
                                    .buildConfigurableContext())
                    .withMessageStartingWith("XML config locations have already been specified");
        }
    }

    @Nested
    class AddXmlConfigLocation {

        @Test
        void shouldAddOneXmlConfigLocation() {
            var context = builder
                    .addXmlConfigLocation("SpringContextBuilderTest/testApplicationContext.xml")
                    .buildConfigurableContext();

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
                                    .buildConfigurableContext())
                    .withMessageStartingWith("Annotated classes have already been specified");
        }
    }

    @Nested
    class WithXmlConfigLocations {

        @Test
        void shouldAddMultipleXmlConfigLocations() {
            var context = builder
                    .withXmlConfigLocations(
                            "SpringContextBuilderTest/testApplicationContext.xml",
                            "SpringContextBuilderTest/secondTestApplicationContext.xml")
                    .buildConfigurableContext();

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
                                    .buildConfigurableContext())
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

            var context = new SpringContextBuilder()
                    .withoutShutdownHooks()
                    .addParentContextBean("myBean", bean)
                    .addAnnotationConfiguration(SampleTestConfiguration.class)
                    .buildConfigurableContext();

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
            var context = builder
                    .withoutShutdownHooks()
                    .addAnnotationConfiguration(SampleTestConfiguration.class)
                    .buildConfigurableContext();

            assertThat(context).isNotNull();
            assertThat(context.getParent()).isNotNull();

            assertThat(context.getBean(OtherTestBean.class)).isNotNull();

            assertCanClose(context);
        }

        @Test
        void shouldDisableShutdownHooks_ForXmlContext() {
            var context = builder
                    .withoutShutdownHooks()
                    .addXmlConfigLocation("SpringContextBuilderTest/testApplicationContext.xml")
                    .buildConfigurableContext();

            assertThat(context).isNotNull();
            assertThat(context.getParent()).isNotNull();

            assertThat(context.getBean(OtherTestBean.class)).isNotNull();

            assertCanClose(context);
        }

    }

    private static void assertCanClose(ConfigurableApplicationContext context) {
        assertThatCode(context::close)
                .describedAs("Closing the context should not throw any exceptions")
                .doesNotThrowAnyException();
    }
}
