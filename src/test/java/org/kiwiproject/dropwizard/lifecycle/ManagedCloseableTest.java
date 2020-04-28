package org.kiwiproject.dropwizard.lifecycle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Closeable;

@DisplayName("ManagedCloseable")
class ManagedCloseableTest {

    private ManagedCloseable managed;
    private Closeable closeable;

    @BeforeEach
    void setUp() {
        closeable = mock(Closeable.class);
        managed = new ManagedCloseable(closeable);
    }

    @Test
    void testStart_IsNoOp() {
        managed.start();
        verifyNoInteractions(closeable);
    }

    @Test
    void testStop_ClosesCloseable() throws Exception {
        managed.stop();
        verify(closeable).close();
    }
}
