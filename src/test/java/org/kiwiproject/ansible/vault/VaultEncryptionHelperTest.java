package org.kiwiproject.ansible.vault;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.collect.KiwiLists.subListExcludingLast;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.base.process.ProcessHelper;
import org.kiwiproject.base.process.Processes;
import org.kiwiproject.collect.KiwiLists;
import org.kiwiproject.internal.Fixtures;
import org.mockito.ArgumentMatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This test mocks out the actual ansible-vault invocations. It therefore tests everything except the
 * actual ansible-vault command execution, which is OK for a unit test.
 * <p>
 * {@link VaultEncryptionHelperIntegrationTest} makes actual calls to ansible-vault, assuming it exists.
 */
@DisplayName("VaultEncryptionHelper")
class VaultEncryptionHelperTest {

    private static final String ENCRYPT_STRING_1_1_FORMAT = "ansible-vault/encrypt_string_1.1.txt";

    // This is the variable name in the above encrypted file
    private static final String VARIABLE_NAME = "db_password";

    @TempDir
    Path folder;

    private VaultEncryptionHelper helper;
    private VaultConfiguration configuration;
    private ProcessHelper processHelper;
    private Process process;

    @BeforeEach
    void setUp() throws IOException {
        var vaultFilePath = Files.createFile(Path.of(folder.toString(), "ansible-vault"));
        var passwordFilePath = Files.createFile(Path.of(folder.toString(), ".vault_pass.txt"));
        Files.writeString(passwordFilePath, "password100");

        configuration = VaultConfiguration.builder()
                .ansibleVaultPath(vaultFilePath.toString())
                .vaultPasswordFilePath(passwordFilePath.toString())
                .tempDirectory(folder.toString())
                .build();

        processHelper = mock(ProcessHelper.class);
        process = mock(Process.class);
        helper = new VaultEncryptionHelper(configuration, processHelper);
    }

    @Nested
    class Constructor {

        @Test
        void shouldNotAllowNullConfig() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new VaultEncryptionHelper(null));
        }

        @Test
        void shouldNotAllowNullProcessHelper() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new VaultEncryptionHelper(mock(VaultConfiguration.class), null));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldValidateVaultPasswordFilePath(String vaultPasswordFilePath) {
            var config = new VaultConfiguration();
            config.setVaultPasswordFilePath(vaultPasswordFilePath);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new VaultEncryptionHelper(config))
                    .withMessageContaining("vaultPasswordFilePath is required");
        }

        @Test
        void shouldValidateVaultPasswordFilePathExists() {
            var config = new VaultConfiguration();
            config.setVaultPasswordFilePath("/almost/certainly/does/not/exist.txt");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new VaultEncryptionHelper(config))
                    .withMessageContaining("vault password file does not exist: %s", config.getVaultPasswordFilePath());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldValidateAnsibleVaultPath(String ansibleVaultPath) {
            var config = new VaultConfiguration();
            config.setAnsibleVaultPath(ansibleVaultPath);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new VaultEncryptionHelper(config))
                    .withMessageContaining("ansibleVaultPath is required");
        }

        @Test
        void shouldValidateAnsibleVaultPathExists() {
            var config = new VaultConfiguration();
            config.setAnsibleVaultPath("/almost/certainly/does/not/exist.txt");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new VaultEncryptionHelper(config))
                    .withMessageContaining("ansible-vault executable does not exist: %s", config.getAnsibleVaultPath());
        }

        @ParameterizedTest
        @CsvSource({
            " , , 'vaultPasswordFilePath is required, ansibleVaultPath is required' ",
            " , /invalid/path/to/ansible-vault, 'vaultPasswordFilePath is required, ansible-vault executable does not exist: /invalid/path/to/ansible-vault' ",
            " /invalid/vault/password/file/path, , 'vault password file does not exist: /invalid/vault/password/file/path, ansibleVaultPath is required'",
            "/invalid/vault/password/file/path , /invalid/path/to/ansible-vault, 'vault password file does not exist: /invalid/vault/password/file/path, ansible-vault executable does not exist: /invalid/path/to/ansible-vault'",
        })
        void shouldReportAllValidationErrors(String vaultPasswordFilePath, String ansibleVaultPath, String expectedMessage) {
            var config = new VaultConfiguration();
            config.setVaultPasswordFilePath(vaultPasswordFilePath);
            config.setAnsibleVaultPath(ansibleVaultPath);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new VaultEncryptionHelper(config))
                    .withMessage(expectedMessage);
        }
    }

    @Nested
    class EncryptFile {

        @Test
        void shouldReturnEncryptedPath_WhenSuccessful() {
            mockOsProcess(processHelper, process, 0, null, "Encryption successful");

            var plainTextFile = "/data/etc/secrets.yml";

            var encryptedFile = helper.encryptFile(plainTextFile);

            assertThat(encryptedFile).isEqualTo(Path.of(plainTextFile));

            var command = VaultEncryptCommand.from(configuration, plainTextFile);
            verify(processHelper).launch(command.parts());
        }

        @Test
        void shouldThrowException_WhenExitCodeIsNonZero() {
            var errorOutput = "ERROR! input is already encrypted";
            mockOsProcess(processHelper, process, 1, null, errorOutput);

            var plainTextFile = "/data/etc/secrets.yml";

            assertThatThrownBy(() -> helper.encryptFile(plainTextFile))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessage("ansible-vault returned non-zero exit code 1. Stderr: %s", errorOutput);

            var command = VaultEncryptCommand.from(configuration, plainTextFile);
            verify(processHelper).launch(command.parts());
        }
    }

    @Nested
    class EncryptFileWithVaultIdLabel {

        @Test
        void shouldReturnEncryptedPath_WhenSuccessful() {
            mockOsProcess(processHelper, process, 0, null, "Encryption successful");

            var vaultIdLabel = "prod";
            var plainTextFile = "/data/etc/prod-secrets.yml";

            var encryptedFile = helper.encryptFile(plainTextFile, vaultIdLabel);

            assertThat(encryptedFile).isEqualTo(Path.of(plainTextFile));

            var command = VaultEncryptCommand.from(configuration, vaultIdLabel, plainTextFile);
            verify(processHelper).launch(command.parts());
        }

        @Test
        void shouldThrowException_WhenExitCodeIsNonZero() {
            var errorOutput = "ERROR! input is already encrypted";
            mockOsProcess(processHelper, process, 1, null, errorOutput);

            var vaultIdLabel = "staging";
            var plainTextFile = "/data/etc/staging-secrets.yml";

            assertThatThrownBy(() -> helper.encryptFile(plainTextFile, vaultIdLabel))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessage("ansible-vault returned non-zero exit code 1. Stderr: %s", errorOutput);

            var command = VaultEncryptCommand.from(configuration, vaultIdLabel, plainTextFile);
            verify(processHelper).launch(command.parts());
        }
    }

    @Nested
    class DecryptFile {

        @Nested
        class InPlace {

            @Test
            void shouldReturnDecryptedPath_WhenSuccessful() {
                mockOsProcess(processHelper, process, 0, null, "Decryption successful");

                var encryptedFile = "/data/etc/secrets.yml";

                var decryptedFile = helper.decryptFile(encryptedFile);

                assertThat(decryptedFile).isEqualTo(Path.of(encryptedFile));

                var command = VaultDecryptCommand.from(configuration, encryptedFile);
                verify(processHelper).launch(command.parts());
            }

            @Test
            void shouldThrowException_WhenExitCodeIsNonZero() {
                var errorOutput = "ERROR! input is not vault encrypted data/etc/secrets.yml is not a vault encrypted file for /etc/secrets.yml";
                mockOsProcess(processHelper, process, 1, null, errorOutput);

                var encryptedFilePath = "/etc/secrets.yml";

                assertThatThrownBy(() -> helper.decryptFile(encryptedFilePath))
                        .isExactlyInstanceOf(VaultEncryptionException.class)
                        .hasMessage("ansible-vault returned non-zero exit code 1. Stderr: %s", errorOutput);

                var command = VaultDecryptCommand.from(configuration, encryptedFilePath);
                verify(processHelper).launch(command.parts());
            }
        }

        @Nested
        class ToNewFile {

            @Test
            void shouldReturnNewPath_WhenSuccessful() {
                mockOsProcess(processHelper, process, 0, null, "Decryption successful");

                var encryptedFile = "/data/crypt/secrets.yml";
                var outputFile = "/data/var/secrets.yml";

                var decryptedFile = helper.decryptFile(encryptedFile, outputFile);

                assertThat(decryptedFile).isEqualTo(Path.of(outputFile));

                var command = VaultDecryptCommand.from(configuration, encryptedFile, outputFile);
                verify(processHelper).launch(command.parts());
            }

            @ParameterizedTest
            @CsvSource({
                    "/data/crypt/secrets.yml,/data/crypt/secrets.yml",
                    "/data/crypt/secrets.yml,/data/crypt/Secrets.yml",
                    "/data/crypt/secrets.yml,/data/crypt/SECRETS.yml"
            })
            void shouldNotPermitNewFileLocationToOverwriteEncryptedFile(String encryptedFile, String outputFile) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> helper.decryptFile(encryptedFile, outputFile))
                        .withMessage("outputFilePath must be different than encryptedFilePath (case-insensitive)");

                verifyNoInteractions(processHelper);
            }

            @Test
            void shouldThrowException_WhenExitCodeIsNonZero() {
                var errorOutput = "ERROR! input is not vault encrypted data/etc/secrets.yml is not a vault encrypted file for /etc/secrets.yml";
                mockOsProcess(processHelper, process, 1, null, errorOutput);

                var encryptedFile = "/data/crypt/secrets.yml";
                var outputFile = "/data/var/secrets.yml";

                assertThatThrownBy(() -> helper.decryptFile(encryptedFile, outputFile))
                        .isExactlyInstanceOf(VaultEncryptionException.class)
                        .hasMessage("ansible-vault returned non-zero exit code 1. Stderr: %s", errorOutput);

                var command = VaultDecryptCommand.from(configuration, encryptedFile, outputFile);
                verify(processHelper).launch(command.parts());
            }
        }
    }

    @Nested
    class ViewFile {

        @Test
        void shouldReturnDecryptedContent_WhenSuccessful() {
            var plainText = "the secret stash";
            mockOsProcess(processHelper, process, 0, plainText, null);

            var encryptedFile = "/data/etc/secrets.yml";

            var decryptedContents = helper.viewFile(encryptedFile);

            assertThat(decryptedContents).isEqualTo(plainText);

            var command = VaultViewCommand.from(configuration, encryptedFile);
            verify(processHelper).launch(command.parts());
        }

        @Test
        void shouldThrowException_WhenExitCodeIsNonZero() {
            var errorOutput = "ERROR! input is not vault encrypted data/etc/secrets.yml is not a vault encrypted file for /etc/secrets.yml";
            mockOsProcess(processHelper, process, 1, null, errorOutput);

            var encryptedFilePath = "/etc/secrets.yml";

            assertThatThrownBy(() -> helper.viewFile(encryptedFilePath))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessage("ansible-vault returned non-zero exit code 1. Stderr: %s", errorOutput);

            var command = VaultViewCommand.from(configuration, encryptedFilePath);
            verify(processHelper).launch(command.parts());
        }
    }

    @Nested
    class RekeyFile {

        @Test
        void shouldRekeyAndReturnPathOfRekeyedFile() {
            mockOsProcess(processHelper, process, 0, null, "Rekey successful");

            var encryptedFile = "/data/etc/secrets.yml";
            var newVaultPasswordFilePath = "~/.new_vault_pass.txt";

            var rekeyedFile = helper.rekeyFile(encryptedFile, newVaultPasswordFilePath);

            assertThat(rekeyedFile).isEqualTo(Path.of(encryptedFile));

            var command = VaultRekeyCommand.from(configuration, encryptedFile, newVaultPasswordFilePath);
            verify(processHelper).launch(command.parts());
        }

        @Test
        void shouldEnsureNewPasswordFileIsDifferentThanOriginalPasswordFile() {
            var encryptedFile = "/data/etc/secrets.yml";
            var newVaultPasswordFilePath = configuration.getVaultPasswordFilePath();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> helper.rekeyFile(encryptedFile, newVaultPasswordFilePath))
                    .withMessage("newVaultPasswordFilePath file must be different than configuration.vaultPasswordFilePath (case-insensitive)");

            verifyNoInteractions(processHelper);
        }

        @Test
        void shouldThrowException_WhenExitCodeIsNonZero() {
            var errorOutput = "ERROR! input is not vault encrypted data/etc/secrets.yml is not a vault encrypted file for /etc/secrets.yml";
            mockOsProcess(processHelper, process, 1, null, errorOutput);

            var encryptedFilePath = "/etc/secrets.yml";
            var newVaultPasswordFilePath = "~/.new_vault_pass.txt";

            assertThatThrownBy(() -> helper.rekeyFile(encryptedFilePath, newVaultPasswordFilePath))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessage("ansible-vault returned non-zero exit code 1. Stderr: %s", errorOutput);

            var command = VaultRekeyCommand.from(configuration, encryptedFilePath, newVaultPasswordFilePath);
            verify(processHelper).launch(command.parts());
        }
    }

    @Nested
    class EncryptString {

        @Test
        void shouldReturnEncryptedString_WhenSuccessful() {
            var encryptedContent = Fixtures.fixture(ENCRYPT_STRING_1_1_FORMAT);

            mockOsProcess(processHelper, process, 0, encryptedContent, "Encryption successful");

            var plainText = "this is the plain text";
            var variableName = "some_variable";
            var result = helper.encryptString(plainText, variableName);

            assertThat(result).isEqualTo(encryptedContent);

            var command = VaultEncryptStringCommand.from(configuration, plainText, variableName);
            verify(processHelper).launch(command.parts());
        }

        @Test
        void shouldThrowException_WhenExitCodeIsNonZero() {
            var errorOutput = "ERROR! input is already encrypted";
            mockOsProcess(processHelper, process, 1, null, errorOutput);

            var plainText = "my-password";
            var variableName = "db_password";
            assertThatThrownBy(() ->
                    helper.encryptString(plainText, variableName))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessage("ansible-vault returned non-zero exit code 1. Stderr: %s", errorOutput);

            var command = VaultEncryptStringCommand.from(configuration, plainText, variableName);
            verify(processHelper).launch(command.parts());
        }
    }

    @Nested
    class EncryptStringWithVaultIdLabel {

        @Test
        void shouldReturnEncryptedString_WhenSuccessful() {
            var encryptedContent = Fixtures.fixture(ENCRYPT_STRING_1_1_FORMAT);

            mockOsProcess(processHelper, process, 0, encryptedContent, "Encryption successful");

            var vaultIdLabel = "dev";
            var plainText = "this is the plain text";
            var variableName = "some_variable";
            var result = helper.encryptString(vaultIdLabel, plainText, variableName);

            assertThat(result).isEqualTo(encryptedContent);

            var command = VaultEncryptStringCommand.from(configuration, vaultIdLabel, plainText, variableName);
            verify(processHelper).launch(command.parts());
        }

        @Test
        void shouldThrowException_WhenExitCodeIsNonZero() {
            var errorOutput = "ERROR! input is already encrypted";
            mockOsProcess(processHelper, process, 1, null, errorOutput);

            var vaultIdLabel = "staging";
            var plainText = "my-password";
            var variableName = "db_password";
            assertThatThrownBy(() ->
                    helper.encryptString(vaultIdLabel, plainText, variableName))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessage("ansible-vault returned non-zero exit code 1. Stderr: %s", errorOutput);

            var command = VaultEncryptStringCommand.from(configuration, vaultIdLabel, plainText, variableName);
            verify(processHelper).launch(command.parts());
        }
    }

    @Nested
    class DecryptString {

        @Test
        void shouldDecryptEncryptedVariable_WhenSuccessful() {
            var plainText = "secret sauce";
            mockOsProcess(processHelper, process, 0, plainText, "Decryption successful");

            var encryptedString = Fixtures.fixture(ENCRYPT_STRING_1_1_FORMAT);

            var result = helper.decryptString(encryptedString);

            assertThat(result).isEqualTo(plainText);

            var encryptedFilePath = Path.of(folder.toString(), VARIABLE_NAME + ".txt");

            // Verify the command that was launched. This is more difficult here due to
            // the way we need to write the encrypt_string content to a temporary file
            // which has a random component in its name to avoid possibility of file name
            // collisions.
            verify(processHelper).launch(argThat(matchesExpectedCommand(encryptedFilePath)));
        }

        private ArgumentMatcher<List<String>> matchesExpectedCommand(Path encryptedFilePath) {
            return commandParts -> {
                // Check command up until last argument (file name, which has a random component)
                var vaultDecryptCommand = VaultDecryptCommand.toStdoutFrom(configuration, encryptedFilePath.toString());
                var vaultDecryptCommandParts = vaultDecryptCommand.parts();
                var expectedPartsExcludingLast = subListExcludingLast(vaultDecryptCommandParts);

                var commandPartsExcludingLast = subListExcludingLast(commandParts);

                assertThat(commandPartsExcludingLast)
                        .describedAs("Command until filename should be the same")
                        .isEqualTo(expectedPartsExcludingLast);

                // Check file name, but ignore the random numbers in the middle of it
                var lastPart = KiwiLists.last(commandParts);
                assertThat(lastPart)
                        .describedAs("File name should start with %s end with .txt", VARIABLE_NAME)
                        .startsWith(Path.of(folder.toString(), VARIABLE_NAME + ".").toString())
                        .endsWith(".txt");

                return true;
            };
        }

        @Test
        void shouldThrowException_WhenExitCodeIsNonZero() {
            var errorOutput = "ERROR! input is already encrypted";
            mockOsProcess(processHelper, process, 1, null, errorOutput);

            var encryptedString = Fixtures.fixture(ENCRYPT_STRING_1_1_FORMAT);

            assertThatThrownBy(() -> helper.decryptString(encryptedString))
                    .isExactlyInstanceOf(VaultEncryptionException.class)
                    .hasMessage("ansible-vault returned non-zero exit code 1. Stderr: %s", errorOutput);

            // Sanity check...
            verify(processHelper).launch(anyList());
        }
    }

    // Things this method mocks:
    //
    // mockProcessHelper:
    // launch (returns mockProcess)
    // waitForExit (return Optional<exitCode>)
    //
    // mockProcess:
    // getInputStream
    // getErrorStream
    private static void mockOsProcess(ProcessHelper mockProcessHelper,
                                      Process mockProcess,
                                      @Nullable Integer exitCode,
                                      @Nullable String stdOutput,
                                      @Nullable String errorOutput) {

        when(mockProcessHelper.launch(anyList())).thenReturn(mockProcess);
        when(mockProcessHelper.waitForExit(same(mockProcess), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.ofNullable(exitCode));

        var stdOutInputStream = newInputStream(stdOutput);
        when(mockProcess.getInputStream()).thenReturn(stdOutInputStream);

        var errorInputStream = newInputStream(errorOutput);
        when(mockProcess.getErrorStream()).thenReturn(errorInputStream);
    }

    private static InputStream newInputStream(@Nullable String value) {
        if (isNull(value)) {
            return InputStream.nullInputStream();
        }

        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    class WhichAnsibleVault {

        /**
         * @implNote We can't assume ansible-vault will exist, so just verify that the result we get
         * is the same as using {@link Processes#which(String)}.
         */
        @Test
        void shouldReturnAnsibleVaultPath_AsString_IfExists() {
            var expectedPath = Processes.which("ansible-vault");
            assertThat(VaultEncryptionHelper.whichAnsibleVault()).isEqualTo(expectedPath);
        }
    }

    @Nested
    class WhichAnsibleVaultAsPath {

        /**
         * @implNote We can't assume ansible-vault will exist, so just verify that the result we get
         * is the same as using {@link Processes#whichAsPath(String)}
         */
        @Test
        void shouldReturnAnsibleVaultPath_AsPathObject_IfExists() {
            var expectedPath = Processes.whichAsPath("ansible-vault");
            assertThat(VaultEncryptionHelper.whichAnsibleVaultAsPath()).isEqualTo(expectedPath);
        }
    }
}
