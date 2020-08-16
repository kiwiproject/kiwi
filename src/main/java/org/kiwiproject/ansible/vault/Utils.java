package org.kiwiproject.ansible.vault;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@UtilityClass
class Utils {

    // TODO This came as part of the ansible.vault package (see #272). Consider:
    //  - moving readProcessXxx methods into KiwiIO

    static String readProcessOutput(Process process) {
        return readInputStreamAsString(process.getInputStream());
    }

    static String readProcessErrorOutput(Process process) {
        return readInputStreamAsString(process.getErrorStream());
    }

    private static String readInputStreamAsString(InputStream inputStream) {
        try {
            var outputStream = new ByteArrayOutputStream();
            inputStream.transferTo(outputStream);
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Error converting InputStream to String", e);
        }
    }
}
