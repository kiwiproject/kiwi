package org.kiwiproject.xml;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgument;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.blankToNull;
import static org.kiwiproject.collect.KiwiMaps.newHashMap;
import static org.kiwiproject.logging.LazyLogParameterSupplier.lazy;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kiwiproject.io.KiwiIO;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * Static utilities for converting to/from XML using JAXB and a few other XML-related utilities.
 *
 * @implNote the appropriate JAXB dependencies must be available at runtime
 */
@UtilityClass
@Slf4j
public class KiwiXml {

    /**
     * Constant that can be used to disable all namespace validations when converting from XML to objects.
     *
     * @see #toObjectAssertingValid(String, Class, NamespaceValidation, List)
     */
    public static final String DISABLE_NAMESPACE_VALIDATION = "*";

    private static final String NAME_KEY = "name";
    private static final String NAMESPACE_KEY = "namespace";
    private static final String PREFIX_MATCH = "([\\w\\d]+:)?";
    private static final List<String> DEFAULT_IGNORED_NAMESPACES = List.of("##default");

    /**
     * Static map that stores mappings from class to {@link JAXBContext}, used to improve performance
     * by re-using existing context objects.
     */
    private static final ConcurrentMap<Class<?>, JAXBContext> jaxbContextMap = new ConcurrentHashMap<>();

    /**
     * Whether validation should be performed or not.
     */
    public enum NamespaceValidation {
        YES,
        NO
    }

    /**
     * Find the classes for which there is a cached {@link JAXBContext}. Each time an instance of a class is
     * converted to XML, the {@link JAXBContext} is retrieved from an internal cache. If there isn't one, then
     * one is created and stored in the cache.
     *
     * @return the set of classes for which {@link KiwiXml} has a cached {@link JAXBContext}. The returned set
     * is an unmodifiable copy of the actual cached classes
     * @implNote The internal cache is a static {@link ConcurrentMap}.
     */
    public static Set<Class<?>> getCachedJAXBContextClasses() {
        return Set.copyOf(jaxbContextMap.keySet());
    }

    /**
     * Clear the internal cache of class to {@link JAXBContext} mappings.
     */
    public static void clearCachedJAXBContextClasses() {
        jaxbContextMap.clear();
    }

    /**
     * Convert the given object to an XML representation.
     *
     * @param object the object to convert to XML
     * @return the XML representation of the object
     */
    public static String toXml(Object object) {
        checkArgumentNotNull(object);
        return toXml(object, object.getClass());
    }

    /**
     * Convert the given object to an XML representation.
     *
     * @param object the object to convert to XML
     * @param clazz  the type of class being converted
     * @return the XML representation of the object
     */
    public static String toXml(Object object, Class<?> clazz) {
        return toXml(object, clazz, Map.of());
    }

    /**
     * Convert the given object to an XML representation.
     *
     * @param object               the object to convert to XML
     * @param clazz                the type of class being converted
     * @param marshallerProperties the properties to be set on the {@link Marshaller} during the conversion process
     * @return the XML representation of the object
     * @see Marshaller#setProperty(String, Object)
     */
    public static String toXml(Object object, Class<?> clazz, Map<String, Object> marshallerProperties) {
        checkArgumentNotNull(object, "object cannot be null");
        checkArgumentNotNull(clazz, "clazz cannot be null");

        try {
            var writer = new StringWriter();
            var context = getJaxbContext(clazz);
            var introspector = context.createJAXBIntrospector();
            var marshaller = createMarshaller(context, marshallerProperties);

            if (isNull(introspector.getElementName(object))) {
                JAXBElement<?> jaxbElement = createJaxbWrappedObject(object);
                marshaller.marshal(jaxbElement, writer);
            } else {
                marshaller.marshal(object, writer);
            }
            return writer.toString();
        } catch (XmlRuntimeException xre) {
            LOG.error("Error converting object to XML", xre);
            throw xre;
        } catch (Exception e) {
            LOG.error("Unknown error converting object to XML", e);
            throw new XmlRuntimeException("Unable to convert to XML", e);
        }
    }

    private static Marshaller createMarshaller(JAXBContext context, Map<String, Object> marshallerProperties)
            throws JAXBException {

        var marshaller = context.createMarshaller();
        Map<String, Object> properties = isNull(marshallerProperties) ?
                new HashMap<>() : new HashMap<>(marshallerProperties);

        properties.putIfAbsent(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        properties.forEach((k, v) -> setMarshallerProperty(marshaller, k, v));

        return marshaller;
    }

    private static void setMarshallerProperty(Marshaller marshaller, String key, Object value) {
        try {
            marshaller.setProperty(key, value);
        } catch (PropertyException e) {
            LOG.error("Encountered exception setting property: {}, with value: {}", key, value);
            throw new XmlRuntimeException("Unable to configure marshaller properties", e);
        }
    }

    private static JAXBElement<?> createJaxbWrappedObject(Object objectToTranslate) {
        var nameAndNamespace = getNameAndNamespace(objectToTranslate.getClass());
        var qName = new QName(nameAndNamespace.get(NAMESPACE_KEY), nameAndNamespace.get(NAME_KEY));

        return new JAXBElement<>(qName, Object.class, objectToTranslate);
    }

    /**
     * Convert the given XML into an object of the specified type.
     *
     * @param xml   the XML to convert
     * @param clazz type of object to convert into
     * @param <T>   the target type
     * @return a new instance of the specified type
     */
    public static <T> T toObject(String xml, Class<T> clazz) {
        return toObjectAssertingValid(xml, clazz, NamespaceValidation.YES);
    }

    /**
     * Convert the given XML into an object of the specified type, ignoring any XML namespace information and not
     * performing any namespace validation.
     *
     * @param xml   the XML to convert
     * @param clazz type of object to convert into
     * @param <T>   the target type
     * @return a new instance of the specified type
     */
    public static <T> T toObjectIgnoringNamespace(String xml, Class<T> clazz) {
        return toObjectAssertingValid(xml, clazz, NamespaceValidation.NO, List.of(DISABLE_NAMESPACE_VALIDATION));
    }

    /**
     * Convert the given XML into an object of the specified type, optionally performing namespace validation.
     *
     * @param xml                 the XML to convert
     * @param clazz               type of object to convert into
     * @param namespaceValidation should namespace validation be performed or not
     * @param <T>                 the target type
     * @return a new instance of the specified type
     */
    public static <T> T toObjectAssertingValid(String xml,
                                               Class<T> clazz,
                                               NamespaceValidation namespaceValidation) {
        return toObjectAssertingValid(xml, clazz, namespaceValidation, DEFAULT_IGNORED_NAMESPACES);
    }

    /**
     * Convert the given XML into an object of the specified type, performing basic validation and ignoring
     * the specified namespaces.
     *
     * @param xml                 the XML to convert
     * @param clazz               type of object to convert into
     * @param namespaceValidation should namespace validation be performed or not
     * @param ignoredNamespaces   list of namespaces to ignore
     * @param <T>                 the target type
     * @return a new instance of the specified type
     */
    public static <T> T toObjectAssertingValid(String xml,
                                               Class<T> clazz,
                                               NamespaceValidation namespaceValidation,
                                               List<String> ignoredNamespaces) {

        checkArgumentNotBlank(xml, "xml cannot be blank");
        checkArgumentNotNull(clazz, "clazz cannot be null");
        try {
            if (namespaceValidation == NamespaceValidation.YES) {
                checkArgument(validateXmlMatchesType(xml, clazz),
                        IllegalArgumentException.class, "XML namespace does not match expected type");
            }
            return tryWithFactory(xml, clazz, ignoredNamespaces);
        } catch (JAXBException e) {
            throw newXmlRuntimeException(e.getLinkedException(), xml);
        } catch (XMLStreamException e) {
            throw newXmlRuntimeException(e.getNestedException(), xml);
        } catch (Exception e) {
            throw newXmlRuntimeException(e, xml);
        }
    }

    /**
     * Validate that the given XML has a namespace that matches the given class, which is generally assumed to
     * be annotated with {@link XmlType}.
     *
     * @param xml   the input XML to compare
     * @param clazz the {@link Class} to compare; assumed to be annotated with {@link XmlType}
     * @param <T>   the type of the target class
     * @return true if the XML namespace (e.g. xmlns) matches the namespace of the {@link XmlType} annotation
     * on the given class
     * @throws XmlRuntimeException if something bad and unexpected happens. The thrown exception wraps a
     *                             {@link XMLStreamException} or other cause.
     */
    public static <T> boolean validateXmlMatchesType(String xml, Class<T> clazz) {
        return validateXmlMatchesType(xml, clazz, DEFAULT_IGNORED_NAMESPACES);
    }

    /**
     * Validate that the given XML has a namespace that matches the given class, which is generally assumed to
     * be annotated with {@link XmlType}, but ignoring the given list of namespaces.
     *
     * @param xml               the input XML to compare
     * @param clazz             the {@link Class} to compare; assumed to be annotated with {@link XmlType}
     * @param ignoredNamespaces the namespaces to ignore
     * @param <T>               the type of the target class
     * @return true if the XML namespace (e.g. xmlns) matches the namespace of the {@link XmlType} annotation
     * on the given class
     * @throws XmlRuntimeException if something bad and unexpected happens. The thrown exception wraps a
     *                             {@link XMLStreamException} or other cause.
     */
    public static <T> boolean validateXmlMatchesType(String xml, Class<T> clazz, List<String> ignoredNamespaces) {
        checkArgumentNotBlank(xml);
        checkArgumentNotNull(clazz);
        checkArgumentNotNull(ignoredNamespaces);

        String xmlns;
        String classNamespace;
        try {
            var xmlNamespaceURI = blankToNull(getRootQualifiedName(xml).getNamespaceURI());
            xmlns = filterIgnoredNamespaces(ignoredNamespaces, xmlNamespaceURI);

            var clazzNamespace = blankToNull(getNameAndNamespace(clazz).get(NAMESPACE_KEY));
            classNamespace = filterIgnoredNamespaces(ignoredNamespaces, clazzNamespace);

            if (StringUtils.equals(xmlns, classNamespace)) {
                LOG.trace("Return true for xmlns: {} and classNamespace: {}", xmlns, classNamespace);
                return true;
            }

            if (isBlank(xmlns) || isBlank(classNamespace)) {
                LOG.info("Skipping validation of namespace for class: {}, with namespace: '{}', and XML with namespace: '{}'." +
                                " One or both are blank or ignored.",
                        clazz.getName(), classNamespace, xmlns);
                return true;
            }
        } catch (Exception e) {
            throw new XmlRuntimeException(e);
        }

        LOG.warn("XML root element with namespace: '{}' does not match expected namespace: '{}', of class: '{}'",
                xmlns, classNamespace, clazz.getName());
        return false;
    }

    private static QName getRootQualifiedName(String xml) throws XMLStreamException {
        XMLStreamReader xmlStreamReader = null;  // not AutoCloseable; cannot use try-with-resources

        try (var stringReader = new StringReader(xml)) {
            var xmlInputFactory = newSecureXMLInputFactory();
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);

            if (xmlStreamReader.hasNext()) {
                var event = xmlStreamReader.next();
                while (event != XMLStreamConstants.START_ELEMENT && xmlStreamReader.hasNext()) {
                    event = xmlStreamReader.next();
                }

                if (xmlStreamReader.isStartElement()) {
                    return xmlStreamReader.getName();
                }
            }

            throw new XmlRuntimeException("Unable to determine root element namespace");
        } finally {
            KiwiIO.closeQuietly(xmlStreamReader);
        }
    }

    private static String filterIgnoredNamespaces(List<String> ignoredNamespaces, String namespace) {
        var shouldIgnore = isNull(namespace) || ignoredNamespaces.contains(namespace);
        if (shouldIgnore) {
            LOG.debug("Ignoring namespace: '{}', treating as null", namespace);
        }
        return shouldIgnore ? null : namespace;
    }

    private static <T> T tryWithFactory(String xml, Class<T> clazz, List<String> ignoredNamespaces)
            throws JAXBException, XMLStreamException {

        XMLStreamReader xmlStreamReader = null;  // not AutoCloseable; cannot use try-with-resources

        try (var stringReader = new StringReader(xml)) {
            var jaxbContext = getJaxbContext(clazz);
            var unmarshaller = jaxbContext.createUnmarshaller();
            var xmlInputFactory = newSecureXMLInputFactory();
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, isNamespaceAware(ignoredNamespaces));
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);
            JAXBElement<T> rootElement = unmarshaller.unmarshal(xmlStreamReader, clazz);
            return rootElement.getValue();
        } finally {
            KiwiIO.closeQuietly(xmlStreamReader);
        }
    }

    private static synchronized JAXBContext getJaxbContext(Class<?> clazz) {
        return jaxbContextMap.computeIfAbsent(clazz, KiwiXml::newJaxbContext);
    }

    private static JAXBContext newJaxbContext(Class<?> clazz) {
        try {
            return JAXBContext.newInstance(clazz);
        } catch (JAXBException e) {
            throw new UncheckedJAXBException("Error creating JAXBContext for " + clazz, e);
        }
    }

    /**
     * Per Sonar rule java:S275 (XML parsers should not be vulnerable to XXE attacks), create a new
     * {@link XMLInputFactory} with external entity processing disabled.
     */
    private static XMLInputFactory newSecureXMLInputFactory() {
        var factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        return factory;
    }

    private static boolean isNamespaceAware(List<String> ignoredNamespaces) {
        return !ignoredNamespaces.contains(DISABLE_NAMESPACE_VALIDATION);
    }

    private static XmlRuntimeException newXmlRuntimeException(Throwable cause, String xml) {
        LOG.trace("Encountered error trying to unmarshal XML: {}", lazy(() -> abbreviate(xml, 256)));

        return new XmlRuntimeException("Unable to unmarshal XML", cause);
    }

    /**
     * Extract XML name and namespace from the given class, assuming it is annotated with {@link XmlType}.
     * <p>
     * If the given class is <em>not</em> annotated with {@link XmlType}, we return the namespace as {@code null}
     * and the name as the "simple name" of the class from {@link Class#getSimpleName()}. This allows some degree
     * of flexibility when working with non-annotated classes, though this should be the exception not the normal
     * situation.
     *
     * @param objectClass the class to get name and namespace from
     * @return a map containing entries for name and namespace
     * @see XmlType#name()
     * @see XmlType#namespace()
     */
    public static Map<String, String> getNameAndNamespace(Class<?> objectClass) {
        checkArgumentNotNull(objectClass);
        return Optional.ofNullable(objectClass.getAnnotation(XmlType.class))
                .map(KiwiXml::getNameAndNamespace)
                .orElseGet(() ->
                        newHashMap(
                                NAMESPACE_KEY, null,
                                NAME_KEY, objectClass.getSimpleName()
                        ));
    }

    private static Map<String, String> getNameAndNamespace(XmlType xmlTypeAnnotation) {
        return newHashMap(
                NAMESPACE_KEY, xmlTypeAnnotation.namespace(),
                NAME_KEY, xmlTypeAnnotation.name()
        );
    }

    /**
     * Removes tags from the given XML but ignoring namespaces.
     *
     * @param xml          the XML containing tags to be removed
     * @param tagsToRemove names of the tags to remove
     * @return XML with the given tags removed
     */
    public static String stripTags(String xml, String... tagsToRemove) {
        var namespacedTagsToRemove = Stream.of(tagsToRemove)
                .map(tag -> PREFIX_MATCH + tag)
                .toArray(String[]::new);
        return stripTagsConsideringNamespace(xml, namespacedTagsToRemove);
    }

    /**
     * Removes tags from the given XML taking into account the full tag name (i.e. possibly including namespace).
     *
     * @param xml          the XML containing tags to be removed
     * @param tagsToRemove names of the tags to remove
     * @return XML with the given tags removed
     */
    public static String stripTagsConsideringNamespace(String xml, String... tagsToRemove) {
        return Arrays.stream(tagsToRemove)
                .reduce(xml, (accumulatedXml, tagToRemove) -> {
                    var tagRegex = "<" + tagToRemove + ">[\\s\\S\\w\\W]*</" + tagToRemove + ">";
                    return accumulatedXml.replaceAll(tagRegex, "");
                });
    }
}
