package org.kiwiproject.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import javax.xml.bind.JAXBElement;
import java.io.IOException;

/**
 * A Jackson {@link JsonSerializer} that serializes {@link JAXBElement} objects, with "nil" values handled by writing
 * a JSON null.
 * <p>
 * Note that jackson-core and jackson-databind must be available at runtime.
 *
 * @see JAXBElement#isNil()
 */
public class JaxbElementSerializer extends JsonSerializer<JAXBElement> {

    @Override
    public void serialize(JAXBElement jaxbElement, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (jaxbElement.isNil()) {
            gen.writeNull();
        } else {
            gen.writeObject(jaxbElement.getValue());
        }
    }
}
