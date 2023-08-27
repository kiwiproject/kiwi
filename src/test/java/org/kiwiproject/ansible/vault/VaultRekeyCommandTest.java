package org.kiwiproject.ansible.vault;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VaultRekeyCommand")
class VaultRekeyCommandTest {

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
        var encryptedFileName = "/data/crypt/passwords.txt";
        var newVaultPasswordFilePath = "/~.ansible/new_vault_pass";

        var command = VaultRekeyCommand.from(configuration, encryptedFileName, newVaultPasswordFilePath);

        assertThat(command.parts()).containsExactly(
                configuration.getAnsibleVaultPath(),
                "rekey",
                "--vault-password-file",
                configuration.getVaultPasswordFilePath(),
                "--new-vault-password-file",
                newVaultPasswordFilePath,
                encryptedFileName
        );

        //noinspection removal
        assertThat(command.getCommandParts()).isEqualTo(command.parts());
    }
}
