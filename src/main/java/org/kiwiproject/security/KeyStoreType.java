package org.kiwiproject.security;

import lombok.Getter;

/**
 * KeyStore types that can be specified when generating an instance of {@link java.security.KeyStore} using
 * {@link java.security.KeyStore#getInstance(String)}.
 *
 * @implNote These are from the Java 11 documentation, specifically from "Java Security Standard Algorithm Names".
 * Also note that while that document lists the types in lower case, the JDK actually uppercases the protocol, so using
 * the actual uppercase enum names here works. At least, it works until and unless they add new protocols that contain
 * things like dots or underscores or other such characters that aren't allowed as enum names. In any case, the types
 * are case-insensitive in the JDK. See the {@link java.security.Provider} class, in the constructor of the nested
 * (and private) {@code ServiceKey} class where the following LOC resides:
 * {@code algorithm = algorithm.toUpperCase(ENGLISH);}
 */
public enum KeyStoreType {

    /**
     * The proprietary keystore implementation provided by the SunJCE provider.
     */
    JCEKS,

    /**
     * The proprietary keystore implementation provided by the SUN provider.
     */
    JKS,

    /**
     * A domain keystore is a collection of keystores presented as a single logical keystore. It is specified by
     * configuration data whose syntax is described in the {@link java.security.DomainLoadStoreParameter} class.
     */
    DKS,

    /**
     * A keystore backed by a PCKS #11 token.
     */
    PKCS11,

    /**
     * The transfer syntax for personal identity information as defined in PKCS #12.
     */
    PKCS12;

    /**
     * The keystore type as a String which can be directly supplied to
     * {@link java.security.KeyStore#getInstance(String)}.
     */
    @Getter
    public final String value;

    KeyStoreType() {
        this.value = name();
    }
}
