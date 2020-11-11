package org.kiwiproject.jsch;

/**
 * Runtime exception that represents SFTP-related errors.
 */
public class SftpTransfersException extends RuntimeException {

    public SftpTransfersException(String message) {
        super(message);
    }

    public SftpTransfersException(String message, Throwable cause) {
        super(message, cause);
    }

    public SftpTransfersException(Throwable cause) {
        super(cause);
    }
}
