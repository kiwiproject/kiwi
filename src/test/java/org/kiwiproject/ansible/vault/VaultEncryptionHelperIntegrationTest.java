package org.kiwiproject.ansible.vault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.firstIfPresent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kiwiproject.base.process.Processes;
import org.kiwiproject.internal.Fixtures;
import org.kiwiproject.io.KiwiIO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * This test only runs on Linux or macOS, and only then if ansible-vault is actually installed
 * in one of the expected locations or was found on the path.
 */
@DisplayName("VaultEncryptionHelper (Integration Test)")
@EnabledOnOs({LINUX, MAC})
@Slf4j
class VaultEncryptionHelperIntegrationTest {

    private static final String ANSIBLE_VAULT_COMMAND = "ansible-vault";

    // Location expected on macOS; assuming installed via Homebrew
    private static final String MACOS_HOMEBREW_ANSIBLE_PATH = "/usr/local/bin/" + ANSIBLE_VAULT_COMMAND;

    // Location expected on Linux; expected installed via yum, apt-get, etc.
    private static final String LINUX_ANSIBLE_PATH = "/usr/bin/" + ANSIBLE_VAULT_COMMAND;

    // Dummy path; will cause JUnit assumption to be false
    private static final String DUMMY_ANSIBLE_PATH = "/dummy/" + ANSIBLE_VAULT_COMMAND;

    private static final String PASSWORD = "password100";

    private static final String THE_SECRET = "Remember to drink your Ovaltine";

    // Cannot be final; set in @BeforeAll based on OS
    private static String ansibleVaultFile;

    private VaultEncryptionHelper helper;

    @TempDir
    Path tempDirPath;

    private String tempDir;

    @BeforeAll
    static void beforeAll() {
        ansibleVaultFile = pathOfAnsibleVault().orElse(DUMMY_ANSIBLE_PATH);
        assumeTrue(Files.exists(Path.of(ansibleVaultFile)), () -> ANSIBLE_VAULT_COMMAND + " not found");
    }

    private static Optional<String> pathOfAnsibleVault() {
        var vaultPath = findAnsibleVaultCommand();

        if (vaultPath.isPresent()) {
            return vaultPath;
        }

        if (SystemUtils.IS_OS_MAC) {
            return Optional.of(MACOS_HOMEBREW_ANSIBLE_PATH);
        } else if (SystemUtils.IS_OS_LINUX) {
            return Optional.of(LINUX_ANSIBLE_PATH);
        }
        return Optional.empty();
    }

    private static Optional<String> findAnsibleVaultCommand() {
        try {
            var proc = Processes.launch("which", ANSIBLE_VAULT_COMMAND);
            var stdOutLines = KiwiIO.readLinesFromInputStreamOf(proc);
            return firstIfPresent(stdOutLines);
        } catch (Exception e) {
            LOG.warn("There was an error executing the 'which {}' command", ANSIBLE_VAULT_COMMAND, e);
            return Optional.empty();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        tempDir = tempDirPath.toString();

        var passwordFilePath = Path.of(tempDir, ".vault_pass");
        Files.writeString(passwordFilePath, PASSWORD);

        var config = VaultConfiguration.builder()
                .ansibleVaultPath(ansibleVaultFile)
                .vaultPasswordFilePath(passwordFilePath.toString())
                .tempDirectory(tempDir)
                .build();

        helper = new VaultEncryptionHelper(config);
    }

    @Nested
    class EncryptFile {

        private Path plainTextFile;

        @BeforeEach
        void setUp() throws IOException {
            plainTextFile = Files.writeString(Path.of(tempDir, "foo.txt"), "the plain text");
        }

        @Test
        void shouldEncryptPlainTextFile() {
            var encryptedFile = helper.encryptFile(plainTextFile);

            assertThat(encryptedFile)
                    .describedAs("Encrypted file path should be the same")
                    .isEqualTo(plainTextFile);
        }

        @Test
        void shouldThrowWhenGivenAlreadyEncryptedFile() {
            var encryptedFile = helper.encryptFile(plainTextFile);

            assertThatThrownBy(() -> helper.encryptFile(encryptedFile))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
        }

        @Test
        void shouldThrowWhenGivenFileThatDoesNotExist() {
            assertThatThrownBy(() -> helper.encryptFile("/does/not/exist.txt"))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
        }
    }

    @Nested
    class EncryptFileWithVaultIdLabel {

        private Path plainTextFile;
        private String vaultIdLabel;

        @BeforeEach
        void setUp() throws IOException {
            plainTextFile = Files.writeString(Path.of(tempDir, "foo.txt"), "the plain text");
            vaultIdLabel = "test";
        }

        @Test
        void shouldEncryptPlainTextFile() throws IOException {
            var encryptedFile = helper.encryptFile(plainTextFile, vaultIdLabel);

            assertThat(encryptedFile)
                    .describedAs("Encrypted file path should be the same")
                    .isEqualTo(plainTextFile);

            var firstLine = first(Files.readAllLines(encryptedFile, StandardCharsets.UTF_8));
            assertThat(firstLine).endsWith(";" + vaultIdLabel);
        }

        @Test
        void shouldThrowWhenGivenAlreadyEncryptedFile() {
            var encryptedFile = helper.encryptFile(plainTextFile);

            assertThatThrownBy(() -> helper.encryptFile(encryptedFile, vaultIdLabel))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
        }

        @Test
        void shouldThrowWhenGivenFileThatDoesNotExist() {
            assertThatThrownBy(() -> helper.encryptFile("/does/not/exist.txt", vaultIdLabel))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
        }
    }

    @Nested
    class DecryptFile {

        private Path encryptedFile;

        @BeforeEach
        void setUp() throws IOException {
            var encryptedResourceFile = Fixtures.fixturePath("ansible-vault/secret.txt");
            encryptedFile = Files.copy(encryptedResourceFile, Path.of(tempDir, "secret.txt"));
        }

        @Nested
        class InPlace {

            @Test
            void shouldDecryptAnEncryptedFileInPlace() throws IOException {
                var decryptedFile = helper.decryptFile(encryptedFile);

                assertThat(decryptedFile)
                        .describedAs("Decrypted file path should be the same")
                        .isEqualTo(encryptedFile);

                var decryptedContents = Files.readString(decryptedFile, StandardCharsets.UTF_8);

                assertThat(decryptedContents).isEqualToNormalizingWhitespace(THE_SECRET);
            }

            @Test
            void shouldThrowWhenGivenAnUnencryptedFile() throws IOException {
                var plainTextFile = Files.writeString(Path.of(tempDir, "foo.txt"), "some plain text");

                assertThatThrownBy(() -> helper.decryptFile(plainTextFile))
                        .isExactlyInstanceOf(VaultEncryptionException.class)
                        .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
            }

            @Test
            void shouldThrowWhenGivenFileThatDoesNotExist() {
                assertThatThrownBy(() -> helper.decryptFile("/does/not/exist.txt"))
                        .isExactlyInstanceOf(VaultEncryptionException.class)
                        .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
            }
        }

        @Nested
        class ToNewFile {

            private Path outputFile;

            @BeforeEach
            void setUp() {
                outputFile = Path.of(tempDir, "new.txt");
            }

            @Test
            void shouldDecryptAnEncryptedFileInPlace() throws IOException {
                var decryptedFile = helper.decryptFile(encryptedFile, outputFile);

                assertThat(decryptedFile)
                        .describedAs("Decrypted file path should be the same")
                        .isEqualTo(outputFile);

                var decryptedContents = Files.readString(outputFile, StandardCharsets.UTF_8);

                assertThat(decryptedContents).isEqualToNormalizingWhitespace(THE_SECRET);
            }

            @Test
            void shouldThrowWhenGivenAnUnencryptedFile() throws IOException {
                var plainTextFile = Files.writeString(Path.of(tempDir, "foo.txt"), "some plain text");

                assertThatThrownBy(() -> helper.decryptFile(plainTextFile, outputFile))
                        .isExactlyInstanceOf(VaultEncryptionException.class)
                        .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
            }

            @Test
            void shouldThrowWhenGivenFileThatDoesNotExist() {
                var missingEncryptedFile = Path.of("/does/not/exist.txt");

                assertThatThrownBy(() -> helper.decryptFile(missingEncryptedFile, outputFile))
                        .isExactlyInstanceOf(VaultEncryptionException.class)
                        .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
            }
        }
    }

    @Nested
    class ViewFile {

        private Path encryptedFile;

        @BeforeEach
        void setUp() throws IOException {
            var encryptedResourceFile = Fixtures.fixturePath("ansible-vault/secret.txt");
            encryptedFile = Files.copy(encryptedResourceFile, Path.of(tempDir, "secret.txt"));
        }

        @Test
        void shouldViewEncryptedFile() {
            var plainText = helper.viewFile(encryptedFile);

            assertThat(plainText).isEqualToNormalizingWhitespace(THE_SECRET);
        }

        @Test
        void shouldNotChangeEncryptedFile() throws IOException {
            var originalEncryptedContent = Files.readString(encryptedFile, StandardCharsets.UTF_8);

            helper.viewFile(encryptedFile);

            assertThat(encryptedFile).hasContent(originalEncryptedContent);
        }

        @Test
        void shouldThrowWhenGivenAnUnencryptedFile() throws IOException {
            var plainTextFile = Files.writeString(Path.of(tempDir, "foo.txt"), "some plain text");

            assertThatThrownBy(() -> helper.viewFile(plainTextFile))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
        }

        @Test
        void shouldThrowWhenGivenFileThatDoesNotExist() {
            assertThatThrownBy(() -> helper.viewFile("/does/not/exist.txt"))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
        }
    }

    @Nested
    class RekeyFile {

        private Path encryptedFile;
        private Path newPasswordFile;
        private VaultConfiguration newConfig;

        @BeforeEach
        void setUp() throws IOException {
            var encryptedResourceFile = Fixtures.fixturePath("ansible-vault/secret.txt");
            encryptedFile = Files.copy(encryptedResourceFile, Path.of(tempDir, "secret.txt"));

            newPasswordFile = Path.of(tempDir, ".new_vault_pass");
            var newPassword = "you'll-shoot-your-eye-out";
            Files.writeString(newPasswordFile, newPassword);

            newConfig = VaultConfiguration.builder()
                    .ansibleVaultPath(ansibleVaultFile)
                    .vaultPasswordFilePath(newPasswordFile.toString())
                    .tempDirectory(tempDir)
                    .build();
        }

        @Test
        void shouldRekeyAnEncryptedFile() {
            var rekeyedFile = helper.rekeyFile(encryptedFile, newPasswordFile);

            assertThatThrownBy(() -> helper.viewFile(rekeyedFile))
                    .describedAs("Should not be able to decrypt using original password file")
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");

            var newHelper = new VaultEncryptionHelper(newConfig);
            var fileContent = newHelper.viewFile(rekeyedFile);
            assertThat(fileContent).isEqualToNormalizingWhitespace(THE_SECRET);
        }

        @Test
        void shouldThrowWhenGivenAnUnencryptedFile() throws IOException {
            var plainTextFile = Files.writeString(Path.of(tempDir, "foo.txt"), "some plain text");

            assertThatThrownBy(() -> helper.rekeyFile(plainTextFile, newPasswordFile))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessageStartingWith("ansible-vault returned non-zero exit code 1. Stderr: ");
        }

        @Test
        void shouldThrowWhenGivenFileThatDoesNotExist() {
            var newPasswordFilePath = newPasswordFile.toString();

            assertThatThrownBy(() -> helper.rekeyFile("/does/not/exist.txt", newPasswordFilePath))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessageStartingWith("ansible-vault returned non-zero exit code");
        }
    }

    @Nested
    class EncryptString {

        @ParameterizedTest
        @CsvSource({
                "my_password,password,1-2-3-4-5",
                "the_answer,42",
                "some_variable,67890-12345"
        })
        void shouldEncryptStringInValidFormat(String variableName, String plainText) {
            var encryptedString = helper.encryptString(plainText, variableName);
            var variable = new VaultEncryptedVariable(encryptedString);

            assertThat(variable.getVariableName()).isEqualTo(variableName);
            assertThat(variable.getVaultIdLabel()).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
                "my_password,this is my password",
                "secret_variable,42",
                "another_variable,12345"
        })
        void shouldEncryptAndDecryptStrings(String variableName, String plainText) {
            var encryptedString = helper.encryptString(plainText, variableName);
            var decryptedString = helper.decryptString(encryptedString);

            assertThat(decryptedString).isEqualTo(plainText);
        }
    }

    @Nested
    class EncryptStringWithVaultIdLabel {

        private String vaultIdLabel;

        @BeforeEach
        void setUp() {
            vaultIdLabel = "dev";
        }

        @ParameterizedTest
        @CsvSource({
                "my_password,password,1-2-3-4-5",
                "the_answer,42",
                "some_variable,67890-12345"
        })
        void shouldEncryptStringInValidFormat(String variableName, String plainText) {
            var encryptedString = helper.encryptString(vaultIdLabel, plainText, variableName);
            var variable = new VaultEncryptedVariable(encryptedString);

            assertThat(variable.getVariableName()).isEqualTo(variableName);
            assertThat(variable.getVaultIdLabel()).hasValue(vaultIdLabel);
        }

        @ParameterizedTest
        @CsvSource({
                "my_password,this is my password",
                "secret_variable,42",
                "another_variable,12345"
        })
        void shouldEncryptAndDecryptStrings(String variableName, String plainText) {
            var encryptedString = helper.encryptString(vaultIdLabel, plainText, variableName);
            var variable = new VaultEncryptedVariable(encryptedString);

            assertThat(variable.getVariableName()).isEqualTo(variableName);
            assertThat(variable.getVaultIdLabel()).hasValue(vaultIdLabel);

            var decryptedString = helper.decryptString(encryptedString);

            assertThat(decryptedString).isEqualTo(plainText);
        }
    }
}