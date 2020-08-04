package org.kiwiproject.ansible.vault;

/**
 * Runtime exception for errors that occur making {@code ansible-vault} calls, but which are not related to invalid
 * method or constructor arguments. Often will wrap an underlying exception such as an {@link java.io.IOException}.
 */
@SuppressWarnings("unused")
public class VaultEncryptionException extends RuntimeException {
    public VaultEncryptionException() {
    }

    public VaultEncryptionException(String message) {
        super(message);
    }

    public VaultEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public VaultEncryptionException(Throwable cause) {
        super(cause);
    }
}
