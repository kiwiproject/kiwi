package org.kiwiproject.ansible.vault;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.requireNotBlank;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration class for creating ansible-vault commands. Can be constructed via the no-arg constructor, the all-args
 * constructor, or the builder. The builder is the preferred way to create an instance if done programmatically.
 * <p>
 * This is mutable in case it is used in injected configuration, e.g., in a Dropwizard configuration file.
 * <p>
 * Has Jakarta Bean Validation annotations to support validation of external configuration, e.g., by Dropwizard's
 * normal validation. But also uses explicit validation in the all-args constructor used for the Lombok builder so
 * that invalid configurations cannot be constructed via the builder.
 */
@Getter
@Setter
public class VaultConfiguration {

    /**
     * Path to the ansible-vault executable.
     */
    @NotBlank
    private String ansibleVaultPath;

    /**
     * Path to the file containing passwords to use when encrypting and decrypting with ansible-vault.
     */
    @NotBlank
    private String vaultPasswordFilePath;

    /**
     * A temporary directory that is used to store encrypted content when decrypting string variables via
     * the {@code ansible-vault encrypt_string} command.
     */
    @NotBlank
    private String tempDirectory;

    /**
     * No-arg constructor. Useful mainly when using an external configuration mechanism that uses setter methods.
     * <p>
     * Sets the temporary directory to the value of the running JVM's {@code java.io.tmpdir} system property.
     */
    public VaultConfiguration() {
        this.tempDirectory = getJavaTempDir();
    }

    /**
     * All-args constructor.
     * <p>
     * If {@code tempDirectory} is blank, the temporary directory will be set to the value of the running JVM's
     * {@code java.io.tmpdir} system property.
     *
     * @param ansibleVaultPath      path to the ansible-vault executable
     * @param vaultPasswordFilePath path to the vault password file for encryption
     * @param tempDirectory         a temporary directory used with the {@code encrypt_string} command
     * @implNote This constructor is used by the Lombok-generated builder, and therefore both this constructor and
     * the builder perform validation on the arguments.
     */
    @Builder
    public VaultConfiguration(String ansibleVaultPath, String vaultPasswordFilePath, String tempDirectory) {
        this.ansibleVaultPath = requireNotBlank(ansibleVaultPath);
        this.vaultPasswordFilePath = requireNotBlank(vaultPasswordFilePath);
        this.tempDirectory = isBlank(tempDirectory) ? getJavaTempDir() : tempDirectory;
    }

    private String getJavaTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Makes a copy of this instance.
     *
     * @return a new instance containing the same values as this instance
     */
    public VaultConfiguration copyOf() {
        return VaultConfiguration.builder()
                .ansibleVaultPath(ansibleVaultPath)
                .vaultPasswordFilePath(vaultPasswordFilePath)
                .tempDirectory(tempDirectory)
                .build();
    }
}
