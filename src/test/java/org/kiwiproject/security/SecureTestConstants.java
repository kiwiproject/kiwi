package org.kiwiproject.security;

import com.google.common.io.Resources;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.nio.file.Path;

@UtilityClass
@Slf4j
public class SecureTestConstants {

    private static final String JKS_FILE_NAME = "unittestkeystore.jks";

    public static final String JKS_FILE_PATH = getJksFilePath();

    public static final String JKS_PASSWORD = "password";

    public static final String STORE_TYPE = "JKS";

    public static final String TLS_PROTOCOL = SSLContextProtocol.TLS_1_3.getValue();

    static {
        LOG.info("Set JKS_FILE_PATH to {}", JKS_FILE_PATH);
    }

    private static String getJksFilePath() {
        try {
            var uri = Resources.getResource(SecureTestConstants.JKS_FILE_NAME).toURI();
            return Path.of(uri).toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
