package org.kiwiproject.ansible.vault;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;

@DisplayName("VaultConfiguration")
class VaultConfigurationTest {

    @Nested
    class Builder {

        @Test
        void shouldUseTempDirectoryIfSupplied() {
            var config = VaultConfiguration.builder()
                    .ansibleVaultPath("/usr/bin/ansible-vault")
                    .vaultPasswordFilePath("/data/vault/.vault_pass")
                    .tempDirectory("/data/vault/tmp")
                    .build();

            assertThat(config.getTempDirectory()).isEqualTo("/data/vault/tmp");
        }

        @Test
        void shouldAssignTempDirectoryIfNotSupplied() {
            var config = VaultConfiguration.builder()
                    .ansibleVaultPath("/usr/bin/ansible-vault")
                    .vaultPasswordFilePath("/data/vault/.vault_pass")
                    .build();

            assertTempDirectoryIsJavaTempDir(config);
        }

        @Test
        void shouldAssignTempDirectoryIfSuppliedAsBlank() {
            var config = VaultConfiguration.builder()
                    .ansibleVaultPath("/usr/bin/ansible-vault")
                    .vaultPasswordFilePath("/data/vault/.vault_pass")
                    .tempDirectory("")
                    .build();

            assertTempDirectoryIsJavaTempDir(config);
        }

        @Test
        void shouldNotAllowBlankAnsibleVaultPath() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> VaultConfiguration.builder()
                            .vaultPasswordFilePath("/data/vault/.vault_pass")
                            .build());
        }

        @Test
        void shouldNotAllowBlankVaultPasswordFilePath() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> VaultConfiguration.builder()
                            .ansibleVaultPath("/usr/bin/ansible-vault")
                            .build());
        }
    }

    @Nested
    class NoArgsConstructor {

        @Test
        void shouldAssignTempDirectory() {
            var config = new VaultConfiguration();

            assertTempDirectoryIsJavaTempDir(config);
        }
    }

    @Nested
    class Copy {

        @Test
        void shouldCreateCopy() {
            var original = VaultConfiguration.builder()
                    .ansibleVaultPath("/usr/bin/ansible-vault")
                    .vaultPasswordFilePath("/data/vault/.vault_pass")
                    .build();

            var copy = original.copyOf();

            assertThat(copy)
                    .isNotSameAs(original)
                    .usingRecursiveComparison()
                    .isEqualTo(original);
        }
    }

    @Nested
    class BeansValidation {

        @Test
        void shouldRequireProperties() {
            var validator = Validation.buildDefaultValidatorFactory().getValidator();
            var emptyConfig = new VaultConfiguration();
            emptyConfig.setTempDirectory(null);

            var violations = validator.validate(emptyConfig);

            var invalidProperties = violations.stream()
                    .map(ConstraintViolation::getPropertyPath)
                    .map(Object::toString)
                    .collect(toSet());

            assertThat(invalidProperties).containsExactlyInAnyOrder(
                    "ansibleVaultPath",
                    "vaultPasswordFilePath",
                    "tempDirectory"
            );
        }
    }

    private void assertTempDirectoryIsJavaTempDir(VaultConfiguration config) {
        assertThat(config.getTempDirectory()).isEqualTo(System.getProperty("java.io.tmpdir"));
    }
}