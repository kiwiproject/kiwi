package org.kiwiproject.ansible.vault;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VaultEncryptCommand")
class VaultEncryptCommandTest {

    private VaultConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = VaultConfiguration.builder()
                .ansibleVaultPath("/usr/bin/ansible-vault")
                .vaultPasswordFilePath("~/.ansible/vault_pass")
                .build();
    }

    @Test
    void shouldBuildCommand() {
        var plainTextFileName = "/data/etc/secrets/passwords.txt";

        var command = VaultEncryptCommand.from(configuration, plainTextFileName);

        assertThat(command.getCommandParts()).containsExactly(
                configuration.getAnsibleVaultPath(),
                "encrypt",
                "--vault-password-file",
                configuration.getVaultPasswordFilePath(),
                plainTextFileName
        );
    }

    @Test
    void shouldBuildCommand_WithVaultId() {
        var vaultIdLabel = "test";
        var plainTextFileName = "/data/etc/secrets/passwords.txt";

        var command = VaultEncryptCommand.from(configuration, vaultIdLabel, plainTextFileName);

        assertThat(command.getCommandParts()).containsExactly(
                configuration.getAnsibleVaultPath(),
                "encrypt",
                "--vault-id",
                vaultIdLabel + "@" + configuration.getVaultPasswordFilePath(),
                plainTextFileName
        );
    }
}