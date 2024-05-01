package org.kiwiproject.ansible.vault;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VaultEncryptStringCommand")
class VaultEncryptStringCommandTest {

    private VaultConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = VaultConfiguration.builder()
                .ansibleVaultPath("/opt/ansible/ansible-vault")
                .vaultPasswordFilePath("~/.vault_pass")
                .build();
    }

    @Test
    void shouldBuildCommand() {
        var plainText = "the plain text";
        var variableName = "MySecret";
        var command = VaultEncryptStringCommand.from(configuration, plainText, variableName);

        assertThat(command.parts()).containsExactly(
                configuration.getAnsibleVaultPath(),
                "encrypt_string",
                "--vault-password-file",
                configuration.getVaultPasswordFilePath(),
                "--name",
                variableName,
                plainText
        );
    }

    @Test
    void shouldBuildCommand_WithVaultId() {
        var vaultIdLabel = "test";
        var plainText = "the plain text";
        var variableName = "MySecret";
        var command = VaultEncryptStringCommand.from(configuration, vaultIdLabel, plainText, variableName);

        assertThat(command.parts()).containsExactly(
                configuration.getAnsibleVaultPath(),
                "encrypt_string",
                "--vault-id",
                vaultIdLabel + "@" + configuration.getVaultPasswordFilePath(),
                "--name",
                variableName,
                plainText
        );
    }
}
