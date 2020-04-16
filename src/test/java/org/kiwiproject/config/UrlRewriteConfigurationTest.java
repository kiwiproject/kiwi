package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UrlRewriteConfigurationTest {

    @Nested
    class NoArgConstructor {

        @Test
        void shouldHaveNullPathPrefix() {
            assertThat(new UrlRewriteConfiguration().getPathPrefix()).isNull();
        }
    }

    @Nested
    class NoneFactoryMethod {

        @Test
        void shouldHaveNullPathPrefix() {
            assertThat(UrlRewriteConfiguration.none().getPathPrefix()).isNull();
        }

        @Test
        void shouldNotRewrite() {
            assertThat(UrlRewriteConfiguration.none().shouldRewrite()).isFalse();
        }
    }

    @Nested
    class ShouldRewrite {

        @Test
        void shouldReturnFalse_WhenPathPrefixIsEmpty() {
            assertThat(new UrlRewriteConfiguration().shouldRewrite()).isFalse();
        }

        @Test
        void shouldReturnFalse_WhenPathPrefixIsBlank() {
            assertThat(UrlRewriteConfiguration.builder().pathPrefix(" ").build().shouldRewrite()).isFalse();
        }

        @Test
        void shouldReturnTrue_WhenPathPrefixIsNotEmpty() {
            var config = UrlRewriteConfiguration.builder()
                    .pathPrefix("/my-proxy")
                    .build();

            assertThat(config.shouldRewrite()).isTrue();
        }
    }
}