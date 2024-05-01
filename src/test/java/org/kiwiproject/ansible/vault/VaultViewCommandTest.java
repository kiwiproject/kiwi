package org.kiwiproject.ansible.vault;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VaultViewCommand")
class VaultViewCommandTest {

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

        var command = VaultViewCommand.from(configuration, encryptedFileName);

        assertThat(command.parts()).containsExactly(
                configuration.getAnsibleVaultPath(),
                "view",
                "--vault-password-file",
                configuration.getVaultPasswordFilePath(),
                encryptedFileName
        );
    }
}
