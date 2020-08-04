package org.kiwiproject.ansible.vault;

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
