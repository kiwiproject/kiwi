package org.kiwiproject.ansible.vault;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.Builder;

import java.nio.file.Paths;
import java.util.List;

/**
 * Generates {@code ansible-vault view} commands.
 */
@Builder
public class VaultViewCommand implements org.kiwiproject.base.process.OsCommand {

    private final String ansibleVaultPath;
    private final String vaultPasswordFilePath;
    private final String encryptedFilePath;

    /**
     * Create an instance.
     *
     * @param configuration     the {@link VaultConfiguration} to use
     * @param encryptedFilePath path to the encrypted file to view
     * @return the command
     */
    public static VaultViewCommand from(VaultConfiguration configuration, String encryptedFilePath) {
        checkArgumentNotNull(configuration, "configuration cannot be null");
        checkArgumentNotBlank(encryptedFilePath, "encryptedFilePath cannot be blank");

        return VaultViewCommand.builder()
                .ansibleVaultPath(configuration.getAnsibleVaultPath())
                .vaultPasswordFilePath(configuration.getVaultPasswordFilePath())
                .encryptedFilePath(encryptedFilePath)
                .build();
    }

    @Override
    public List<String> parts() {
        return List.of(
                ansibleVaultPath,
                "view",
                "--vault-password-file", vaultPasswordFilePath,
                Paths.get(encryptedFilePath).toString()
        );
    }
}
