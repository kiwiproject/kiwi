package org.kiwiproject.collect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MapTypeMismatchException")
class MapTypeMismatchExceptionTest {

    @Test
    void shouldConstructWithNoArgs() {
        var ex = new MapTypeMismatchException();
        
        assertThat(ex.getMessage()).isNull();
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void shouldConstructWithMessage() {
        var message = "Cannot cast map value";
        var ex = new MapTypeMismatchException(message);
        
        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void shouldConstructWithMessageAndCause() {
        var message = "Cannot cast map value";
        var cause = new ClassCastException("Cannot cast String to Integer");
        var ex = new MapTypeMismatchException(message, cause);
        
        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void shouldConstructWithCause() {
        var cause = new ClassCastException("Cannot cast String to Integer");
        var ex = new MapTypeMismatchException(cause);
        
        assertThat(ex.getMessage()).contains(cause.toString());
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void shouldCreateForTypeMismatchWithKeyAndValueTypeAndCause() {
        var key = "count";
        var valueType = Integer.class;
        var cause = new ClassCastException("Cannot cast String to Integer");
        
        var ex = MapTypeMismatchException.forTypeMismatch(key, valueType, cause);
        
        assertThat(ex.getMessage()).isEqualTo("Cannot cast value for key 'count' to type java.lang.Integer");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
