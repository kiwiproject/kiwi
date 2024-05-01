package org.kiwiproject.ansible.vault;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import lombok.Builder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * Generates {@code ansible-vault encrypt_string} commands.
 */
@Builder
public class VaultEncryptStringCommand implements org.kiwiproject.base.process.OsCommand {

    private final String ansibleVaultPath;
    private final String vaultIdLabel;
    private final String vaultPasswordFilePath;
    private final String variableName;
    private final String plainText;

    /**
     * Create an instance.
     *
     * @param configuration the {@link VaultConfiguration} to use
     * @param plainText     the plain text to encrypt
     * @param variableName  the name of the variable
     * @return the command
     */
    public static VaultEncryptStringCommand from(VaultConfiguration configuration,
                                                 String plainText,
                                                 String variableName) {
        return from(configuration, null, plainText, variableName);
    }

    /**
     * Create an instance.
     *
     * @param configuration the {@link VaultConfiguration} to use
     * @param vaultIdLabel  the label of the vault (for use with the {@code --vault-id} argument
     * @param plainText     the plain text to encrypt
     * @param variableName  the name of the variable
     * @return the command
     */
    public static VaultEncryptStringCommand from(VaultConfiguration configuration,
                                                 @Nullable String vaultIdLabel,
                                                 String plainText,
                                                 String variableName) {
        checkArgumentNotNull(configuration, "configuration cannot be null");
        checkArgumentNotBlank(plainText, "plainText cannot be blank");
        checkArgumentNotBlank(variableName, "variableName cannot be blank");

        return VaultEncryptStringCommand.builder()
                .ansibleVaultPath(configuration.getAnsibleVaultPath())
                .vaultIdLabel(vaultIdLabel)
                .vaultPasswordFilePath(configuration.getVaultPasswordFilePath())
                .variableName(variableName)
                .plainText(plainText)
                .build();
    }

    @Override
    public List<String> parts() {
        if (isNull(vaultIdLabel)) {
            return List.of(
                    ansibleVaultPath,
                    "encrypt_string",
                    "--vault-password-file", vaultPasswordFilePath,
                    "--name", variableName,
                    plainText
            );
        }

        return List.of(
                ansibleVaultPath,
                "encrypt_string",
                "--vault-id", vaultIdArgument(),
                "--name", variableName,
                plainText
        );
    }

    private String vaultIdArgument() {
        return f("{}@{}", vaultIdLabel, vaultPasswordFilePath);
    }
}
