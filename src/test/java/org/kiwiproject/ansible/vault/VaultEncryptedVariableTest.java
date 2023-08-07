package org.kiwiproject.ansible.vault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.SOLARIS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.internal.Fixtures;

import java.nio.file.Path;
import java.util.regex.Pattern;

@DisplayName("VaultEncryptedVariable")
class VaultEncryptedVariableTest {

    private static final Pattern NUMBERS_ONLY_PATTERN = Pattern.compile("[0-9]+");

    /**
     * @implNote Only enabled on Linux, macOS, and Solaris due to hardcoded line separator in @ValueSource
     * (limitation is because Java requires a constant value in annotation values)
     */
    @Nested
    @EnabledOnOs({LINUX, MAC, SOLARIS})
    class Constructor {

        @Nested
        class ShouldThrowIllegalArgumentException {

            @ParameterizedTest
            @ValueSource(strings = {
                    "db_password: !vault |\n",
                    "db_password: !vault |\n          $ANSIBLE_VAULT;1.1;AES256",
            })
            void whenDoesNotHaveMoreThanTwoLines(String input) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> new VaultEncryptedVariable(input))
                        .withMessage("Input does not appear to be valid encrypt_string content");
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "db_password:\n          $ANSIBLE_VAULT;1.1;AES256\n          63323...",  // missing !vault
                    "db_password !vault |\n          $ANSIBLE_VAULT;1.1;AES256\n          63323...",  // missing colon
                    "db_password: !vault\n          $ANSIBLE_VAULT;1.1;AES256\n          63323...",  // missing ending |
                    "db_password: !Vault |\n          $ANSIBLE_VAULT;1.1;AES256\n          63323..."  // vault is capitalized
            })
            void whenFirstLineIsInvalid(String input) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> new VaultEncryptedVariable(input))
                        .withMessage("First line does not have a valid variable name declaration");
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "db_password: !vault |\n$ANSIBLE_VAULT;1.1;AES256\n          63323...",  // no spaces
                    "db_password: !vault |\n         $ANSIBLE_VAULT;1.1;AES256\n          63323...",  // only 9 spaces
                    "db_password: !vault |\n            $ANSIBLE_VAULT;1.1;AES256\n          63323...",  // 11 spaces
                    "db_password: !vault |\n          $ANSIBLE_VAULT;1.1;AES256; \n          63323...",  // blank vault ID label
                    "db_password: !vault |\n          $ANSIBLE_VAULT;1.3;AES256\n          63323...",  // invalid format
            })
            void whenSecondLineIsInvalid(String input) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> new VaultEncryptedVariable(input))
                        .withMessage("Second line does not have a valid $ANSIBLE_VAULT declaration");
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "db_password: !vault |\n          $ANSIBLE_VAULT;1.1;AES256\n63323...",  // no spaces
                    "db_password: !vault |\n          $ANSIBLE_VAULT;1.1;AES256\n         63323..."  // only 9 spaces
            })
            void whenRemainingLinesDoNotStartWithTenSpaces(String input) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> new VaultEncryptedVariable(input))
                        .withMessage("Encrypted content does not start with 10 spaces");
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "db_password:: !vault |\n          $ANSIBLE_VAULT;1.1;AES256\n           63323...",  // 11 spaces
                    "db_password:: !vault |\n          $ANSIBLE_VAULT;1.1;AES256\n          63323...\n           39373"  // 11 spaces in second line of encrypted content
            })
            void whenRemainingLinesStartsWithMoreThanTenSpaces(String input) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> new VaultEncryptedVariable(input))
                        .withMessage("Encrypted content is not longer than 10 characters or has more than 10 spaces before encrypted content");
            }
        }

        @ParameterizedTest
        @CsvSource({
                "1.1, db_password",
                "1.2, some_password"
        })
        void shouldParseVariableName(String formatVersion, String expectedVariableName) {
            var encryptString = fixtureWithFormatVersion(formatVersion);
            var vaultEncryptedVariable = new VaultEncryptedVariable(encryptString);

            assertThat(vaultEncryptedVariable.getVariableName()).isEqualTo(expectedVariableName);
        }

        @ParameterizedTest
        @ValueSource(strings = {"1.1", "1.2"})
        void shouldParseFormatVersion(String formatVersion) {
            var encryptString = fixtureWithFormatVersion(formatVersion);
            var vaultEncryptedVariable = new VaultEncryptedVariable(encryptString);

            assertThat(vaultEncryptedVariable.getFormatVersion()).isEqualTo(formatVersion);
        }

        @ParameterizedTest
        @CsvSource({
                "1.1, AES256",
                "1.2, AES256"
        })
        void shouldParseCipher(String formatVersion, String expectedCipher) {
            var encryptString = fixtureWithFormatVersion(formatVersion);
            var vaultEncryptedVariable = new VaultEncryptedVariable(encryptString);

            assertThat(vaultEncryptedVariable.getCipher()).isEqualTo(expectedCipher);
        }

        @Nested
        class ShouldParseVaultIdLabel {

            @Test
            void asEmpty_WhenFormatVersion_1_1() {
                var encryptString = fixtureWithFormatVersion("1.1");
                var vaultEncryptedVariable = new VaultEncryptedVariable(encryptString);

                assertThat(vaultEncryptedVariable.getVaultIdLabel()).isEmpty();
            }

            @Test
            void whenFormatVersion_1_2() {
                var encryptString = fixtureWithFormatVersion("1.2");
                var vaultEncryptedVariable = new VaultEncryptedVariable(encryptString);

                assertThat(vaultEncryptedVariable.getVaultIdLabel()).hasValue("dev");
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"1.1", "1.2"})
        void shouldParseEncryptedContent(String formatVersion) {
            var encryptString = fixtureWithFormatVersion(formatVersion);
            var vaultEncryptedVariable = new VaultEncryptedVariable(encryptString);

            var encryptedContent = Fixtures.fixture("ansible-vault/encrypt_string_" + formatVersion + "_encrypted_content_only.txt")
                    .lines()
                    .toList();
            assertThat(vaultEncryptedVariable.getEncryptedContentLines()).isEqualTo(encryptedContent);
        }
    }

    private static String fixtureWithFormatVersion(String version) {
        return Fixtures.fixture("ansible-vault/encrypt_string_" + version + ".txt");
    }

    @Nested
    class GenerateRandomFilePath {

        @TempDir
        Path tempDir;

        private String tempDirectory;

        @BeforeEach
        void setUp() {
            tempDirectory = tempDir.toString();
        }

        @RepeatedTest(50)
        void shouldGenerateFilesWithRandomishNames() {
            var filePath = VaultEncryptedVariable.generateRandomFilePath(tempDirectory, "db_password");

            assertThat(filePath).startsWithRaw(tempDir);

            var fileName = filePath.getFileName().toString();

            var nameParts = fileName.split("\\.");
            assertThat(nameParts).hasSize(3);
            assertThat(nameParts[0]).isEqualTo("db_password");
            assertThat(nameParts[1]).containsPattern(NUMBERS_ONLY_PATTERN);
            assertThat(nameParts[2]).isEqualTo("txt");
        }
    }
}
