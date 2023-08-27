package org.kiwiproject.ansible.vault;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.Builder;
import org.kiwiproject.base.KiwiDeprecated;

import java.nio.file.Paths;
import java.util.List;

/**
 * Generates {@code ansible-vault rekey} commands.
 */
@Builder
public class VaultRekeyCommand implements org.kiwiproject.base.process.OsCommand {

    private final String ansibleVaultPath;
    private final String vaultPasswordFilePath;
    private final String encryptedFilePath;
    private final String newVaultPasswordFilePath;

    /**
     * Create an instance.
     *
     * @param configuration            the {@link VaultConfiguration} to use
     * @param encryptedFilePath        path to the encrypted file to re-key
     * @param newVaultPasswordFilePath path to the vault password file containing the new password
     * @return the command
     */
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
        return List.of(
                ansibleVaultPath,
                "rekey",
                "--vault-password-file", vaultPasswordFilePath,
                "--new-vault-password-file", newVaultPasswordFilePath,
                Paths.get(encryptedFilePath).toString()
        );
    }
}
