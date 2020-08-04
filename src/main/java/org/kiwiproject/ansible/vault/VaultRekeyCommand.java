package org.kiwiproject.ansible.vault;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.Builder;

import java.nio.file.Paths;
import java.util.List;

@Builder
public class VaultRekeyCommand implements OsCommand {

    private final String ansibleVaultPath;
    private final String vaultPasswordFilePath;
    private final String encryptedFilePath;
    private final String newVaultPasswordFilePath;

    public static VaultRekeyCommand from(VaultConfiguration configuration,
                                         String encryptedFilePath,
                                         String newVaultPasswordFilePath) {
        checkArgumentNotNull(configuration, "configuration cannot be null");
        checkArgumentNotBlank(encryptedFilePath, "encryptedFilePath cannot be blank");
        checkArgumentNotBlank(newVaultPasswordFilePath, "newVaultPasswordFilePath cannot be blank");

        return VaultRekeyCommand.builder()
                .ansibleVaultPath(configuration.getAnsibleVaultPath())
                .vaultPasswordFilePath(configuration.getVaultPasswordFilePath())
                .encryptedFilePath(encryptedFilePath)
                .newVaultPasswordFilePath(newVaultPasswordFilePath)
                .build();
    }

    @Override
    public List<String> getCommandParts() {
        return List.of(
                ansibleVaultPath,
                "rekey",
                "--vault-password-file", vaultPasswordFilePath,
                "--new-vault-password-file", newVaultPasswordFilePath,
                Paths.get(encryptedFilePath).toString()
        );
    }
}
