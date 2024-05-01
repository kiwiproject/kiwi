package org.kiwiproject.ansible.vault;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("VaultDecryptCommand")
class VaultDecryptCommandTest {

    private VaultConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = VaultConfiguration.builder()
                .ansibleVaultPath("/opt/ansible/ansible-vault")
                .vaultPasswordFilePath("~/.vault_pass")
                .tempDirectory("/opt/ansible/tmp")
                .build();
    }

    @Nested
    class ShouldBuildCommand {

        @Test
        void whenGivenNoOutputFile() {
            var encryptedFilePath = "/data/secret/MySecret.txt";
            var command = VaultDecryptCommand.from(configuration, encryptedFilePath);

            assertThat(command.parts()).containsExactly(
                    configuration.getAnsibleVaultPath(),
                    "decrypt",
                    "--vault-password-file",
                    configuration.getVaultPasswordFilePath(),
                    encryptedFilePath
            );
        }

        @Test
        void whenGivenOutputFile() {
            var encryptedFilePath = "/data/secret/MySecret.txt";
            var outputFilePath = "/data/temp/Plain.txt";
            var command = VaultDecryptCommand.from(configuration, encryptedFilePath, outputFilePath);

            assertThat(command.parts()).containsExactly(
                    configuration.getAnsibleVaultPath(),
                    "decrypt",
                    "--vault-password-file",
                    configuration.getVaultPasswordFilePath(),
                    "--output",
                    outputFilePath,
                    encryptedFilePath
            );
        }

        @Test
        void whenGivenStdOutAsOutputFile() {
            var encryptedFilePath = "/data/secret/MySecret.txt";
            var command = VaultDecryptCommand.toStdoutFrom(configuration, encryptedFilePath);

            assertThat(command.parts()).containsExactly(
                    configuration.getAnsibleVaultPath(),
                    "decrypt",
                    "--vault-password-file",
                    configuration.getVaultPasswordFilePath(),
                    "--output",
                    "-",
                    encryptedFilePath
            );
        }
    }
}
