package org.kiwiproject.ansible.vault;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.Builder;

import javax.annotation.Nullable;
import java.nio.file.Paths;
import java.util.List;

@Builder
public class VaultDecryptCommand implements OsCommand {

    public static final String OUTPUT_FILE_STDOUT = "-";

    private final String ansibleVaultPath;
    private final String vaultPasswordFilePath;
    private final String encryptedFilePath;
    private final String outputFilePath;

    public static VaultDecryptCommand from(VaultConfiguration configuration, String encryptedFilePath) {
        return from(configuration, encryptedFilePath, null);
    }

    public static VaultDecryptCommand toStdoutFrom(VaultConfiguration configuration, String encryptedFilePath) {
        return from(configuration, encryptedFilePath, OUTPUT_FILE_STDOUT);
    }

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

    @Override
    public List<String> getCommandParts() {
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
