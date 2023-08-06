package org.kiwiproject.ansible.vault;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.joining;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.collect.KiwiLists.subListFrom;

import lombok.AccessLevel;
import lombok.Getter;
import org.kiwiproject.collect.KiwiLists;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a variable encrypted using {@code ansible-vault encrypt_string}.
 * <p>
 * The constructor attempts to parse an ansible-vault encrypted variable, and will throw an
 * {@link IllegalArgumentException} if the given encrypted string is not a valid ansible vault encrypted variable.
 * <p>
 * Assuming the encrypted input is value, the resulting instance provides accessor methods for the various
 * components of the encrypted variable. These include variable name, ansible vault format version, cipher,
 * vault ID label (optional), as well as the encrypted contents.
 * <p>
 * This is package-private as there is currently no reason for it to be public.
 */
@Getter(AccessLevel.PACKAGE)
class VaultEncryptedVariable {

    private static final String INVALID_ENCRYPT_STRING_INPUT =
            "Input does not appear to be valid encrypt_string content";

    private static final String INVALID_VARIABLE_NAME_DECLARATION =
            "First line does not have a valid variable name declaration";

    private static final String INVALID_ANSIBLE_VAULT_DECLARATION =
            "Second line does not have a valid $ANSIBLE_VAULT declaration";

    private static final String INVALID_SPACING_IN_ENCRYPTED_CONTENT =
            "Encrypted content does not start with 10 spaces";

    private static final String INVALID_FORMAT_IN_ENCRYPTED_CONTENT =
            "Encrypted content is not longer than 10 characters or has more than 10 spaces before encrypted content";

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private String variableName;
    private String formatVersion;
    private String cipher;
    @Getter(AccessLevel.NONE) private String vaultIdLabel;
    private List<String> encryptedContentLines;
    private final String encryptedFileContent;

    VaultEncryptedVariable(String encryptedString) {
        checkArgumentNotBlank(encryptedString, "encryptedString cannot be blank");
        parse(encryptedString);
        this.encryptedFileContent = buildEncryptedFileContent();
    }

    private void parse(String encryptStringOutput) {
        var lines = encryptStringOutput.lines().toList();
        checkArgument(lines.size() > 2, INVALID_ENCRYPT_STRING_INPUT);

        parseLine1(lines);
        parseLine2(lines);
        parseRemainingLines(lines);
    }

    // line 1 should be:
    // <variable_name>: !vault |
    private void parseLine1(List<String> lines) {
        var first = KiwiLists.first(lines);
        checkArgument(first.endsWith(": !vault |"), INVALID_VARIABLE_NAME_DECLARATION);

        this.variableName = first.split(":")[0];
    }

    // line 2 should be:
    // <10 spaces>$ANSIBLE_VAULT;<format-version>;<cipher>[;<vault-id-label]
    private void parseLine2(List<String> lines) {
        var second = KiwiLists.second(lines);
        checkArgument(second.contains(";"), INVALID_ANSIBLE_VAULT_DECLARATION);

        var splat = second.split(";");
        checkArgument(line2HasValidLength(splat) && line2HasValidPrefix(splat[0]), INVALID_ANSIBLE_VAULT_DECLARATION);

        checkArgument(isValidFormatVersion(splat[1]), INVALID_ANSIBLE_VAULT_DECLARATION);
        this.formatVersion = splat[1];

        checkArgumentNotBlank(splat[2], INVALID_ANSIBLE_VAULT_DECLARATION);
        this.cipher = splat[2];

        if (splat.length == 4) {
            checkArgumentNotBlank(splat[3], INVALID_ANSIBLE_VAULT_DECLARATION);
            this.vaultIdLabel = splat[3];
        }
    }

    private static boolean line2HasValidPrefix(String value) {
        return "          $ANSIBLE_VAULT".equals(value);
    }

    private static boolean line2HasValidLength(String[] parts) {
        return parts.length == 3 || parts.length == 4;
    }

    private static boolean isValidFormatVersion(String value) {
        return "1.1".equals(value) || "1.2".equals(value);
    }

    // lines 3-N should be:
    // <10 spaces><cipher text>
    private void parseRemainingLines(List<String> lines) {
        var remainingLines = subListFrom(lines, 3);
        remainingLines.forEach(line -> {
            checkArgument(line.startsWith("          "), INVALID_SPACING_IN_ENCRYPTED_CONTENT);
            checkArgument(line.length() > 10 && line.charAt(10) != ' ', INVALID_FORMAT_IN_ENCRYPTED_CONTENT);
        });
        this.encryptedContentLines = remainingLines;
    }

    private String buildEncryptedFileContent() {
        return encryptedFileFirstLine() +
                LINE_SEPARATOR +
                encryptedContentWithLeadingWhitespaceTrimmed();
    }

    private String encryptedFileFirstLine() {
        return "$ANSIBLE_VAULT;" + formatVersion + ";" + cipher + vaultIdLabelFragmentOrEmpty();
    }

    private String vaultIdLabelFragmentOrEmpty() {
        return Optional.ofNullable(vaultIdLabel)
                .map(label -> ";" + label)
                .orElse("");
    }

    private String encryptedContentWithLeadingWhitespaceTrimmed() {
        return encryptedContentLines.stream()
                .map(String::stripLeading)
                .collect(joining(LINE_SEPARATOR));
    }

    byte[] getEncryptedFileBytes() {
        return encryptedFileContent.getBytes(StandardCharsets.UTF_8);
    }

    Path generateRandomFilePath(String tempDirectoryPath) {
        return VaultEncryptedVariable.generateRandomFilePath(tempDirectoryPath, variableName);
    }

    static Path generateRandomFilePath(String tempDirectoryPath, String variableName) {
        return Path.of(tempDirectoryPath, generateRandomFileName(variableName));
    }

    private static String generateRandomFileName(String variableName) {
        return variableName +
                "." +
                Integer.toUnsignedString(ThreadLocalRandom.current().nextInt()) +
                Long.toUnsignedString(ThreadLocalRandom.current().nextLong()) +
                ".txt";
    }

    Optional<String> getVaultIdLabel() {
        return Optional.ofNullable(vaultIdLabel);
    }
}
