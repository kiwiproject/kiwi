package org.kiwiproject.ansible.vault;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.Builder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.base.KiwiDeprecated;

import java.nio.file.Paths;
import java.util.List;

/**
 * Generates {@code ansible-vault decrypt} commands.
 */
@Builder
public class VaultDecryptCommand implements org.kiwiproject.base.process.OsCommand {

    public static final String OUTPUT_FILE_STDOUT = "-";

    private final String ansibleVaultPath;
    private final String vaultPasswordFilePath;
    private final String encryptedFilePath;
    private final String outputFilePath;

    /**
     * Create an instance.
     *
     * @param configuration     the {@link VaultConfiguration} to use
     * @param encryptedFilePath path to the encrypted file
     * @return the command
     */
    public static VaultDecryptCommand from(VaultConfiguration configuration, String encryptedFilePath) {
        return from(configuration, encryptedFilePath, null);
    }

    /**
     * Create an instance.
     *
     * @param configuration     the {@link VaultConfiguration} to use
     * @param encryptedFilePath path to the encrypted file
     * @return the command
     */
    public static VaultDecryptCommand toStdoutFrom(VaultConfiguration configuration, String encryptedFilePath) {
        return from(configuration, encryptedFilePath, OUTPUT_FILE_STDOUT);
    }

    /**
     * @param configuration     the {@link VaultConfiguration} to use
     * @param encryptedFilePath path to the encrypted file
     * @param outputFilePath    path of the file where the decrypted output should be stored
     * @return the command
     */
    public static VaultDecryptCommand from(VaultConfiguration configuration,
                                           String encryptedFilePath,
                                           @Nullable String outputFilePath) {
        checkArgumentNotNull(configuration, "configuration cannot be null");
        checkArgumentNotBlank(encryptedFilePath, "encryptedFilePath cannot be blank");

        return VaultDecryptCommand.builder()
                .ansibleVaultPath(configuration.getAnsibleVaultPath())
                .vaultPasswordFilePath(configuration.getVaultPasswordFilePath())
                .encryptedFilePath(encryptedFilePath)
                .outputFilePath(outputFilePath)
                .build();
    }

    /**
     * @return a list containing the command and its arguments
     * @deprecated replaced by {@link #parts()}
     */
    @Deprecated(since = "3.1.0", forRemoval = true)
    @KiwiDeprecated(
            removeAt = "4.0.0",
            reference = "https://github.com/kiwiproject/kiwi/issues/1026",
            replacedBy = "#parts"
    )
    public List<String> getCommandParts() {
        return parts();
    }

    @Override
    public List<String> parts() {
        if (nonNull(outputFilePath)) {
            return getCommandPartsWithOutputFile();
        }

        return getCommandPartsWithNoOutputFile();
    }

    private List<String> getCommandPartsWithOutputFile() {
        return List.of(
                ansibleVaultPath,
                "decrypt",
                "--vault-password-file", vaultPasswordFilePath,
                "--output", outputFilePath,
                Paths.get(encryptedFilePath).toString()
        );
    }

    private List<String> getCommandPartsWithNoOutputFile() {
        return List.of(
                ansibleVaultPath,
                "decrypt",
                "--vault-password-file", vaultPasswordFilePath,
                Paths.get(encryptedFilePath).toString()
        );
    }
}
