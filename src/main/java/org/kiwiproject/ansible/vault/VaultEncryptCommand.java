package org.kiwiproject.ansible.vault;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import lombok.Builder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.base.KiwiDeprecated;

import java.util.List;

/**
 * Generates {@code ansible-vault encrypt} commands.
 */
@Builder
public class VaultEncryptCommand implements org.kiwiproject.base.process.OsCommand {

    private final String ansibleVaultPath;
    private final String vaultIdLabel;
    private final String vaultPasswordFilePath;
    private final String plainTextFilePath;

    /**
     * Create an instance.
     *
     * @param configuration     the {@link VaultConfiguration} to use
     * @param plainTextFilePath path to the plain text file to encrypt
     * @return the command
     */
    public static VaultEncryptCommand from(VaultConfiguration configuration, String plainTextFilePath) {
        return from(configuration, null, plainTextFilePath);
    }

    /**
     * Create an instance.
     *
     * @param configuration     the {@link VaultConfiguration} to use
     * @param vaultIdLabel      the label of the vault (for use with the {@code --vault-id} argument
     * @param plainTextFilePath path to the plain text file to encrypt
     * @return the command
     */
    public static VaultEncryptCommand from(VaultConfiguration configuration,
                                           @Nullable String vaultIdLabel,
                                           String plainTextFilePath) {
        checkArgumentNotNull(configuration, "configuration cannot be null");
        checkArgumentNotBlank(plainTextFilePath, "plainTextFilePath cannot be blank");

        return VaultEncryptCommand.builder()
                .ansibleVaultPath(configuration.getAnsibleVaultPath())
                .vaultIdLabel(vaultIdLabel)
                .vaultPasswordFilePath(configuration.getVaultPasswordFilePath())
                .plainTextFilePath(plainTextFilePath)
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
        if (isNull(vaultIdLabel)) {
            return List.of(
                    ansibleVaultPath,
                    "encrypt",
                    "--vault-password-file", vaultPasswordFilePath,
                    plainTextFilePath
            );
        }

        return List.of(
                ansibleVaultPath,
                "encrypt",
                "--vault-id", vaultIdArgument(),
                plainTextFilePath
        );
    }

    private String vaultIdArgument() {
        return f("{}@{}", vaultIdLabel, vaultPasswordFilePath);
    }
}
