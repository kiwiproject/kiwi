package org.kiwiproject.ansible.vault;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.requireNotBlank;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * This is mutable in case it is used in injected configuration, e.g. in a Dropwizard configuration file.
 * <p>
 * Has beans validation annotations to support validation of external configuration, e.g. by Dropwizard's
 * normal validation. But also uses explicit validation in the constructor used for the Lombok builder so
 * that invalid configurations cannot be constructed via the builder.
 */
@Getter
@Setter
public class VaultConfiguration {

    @NotBlank
    private String ansibleVaultPath;

    @NotBlank
    private String vaultPasswordFilePath;

    @NotBlank
    private String tempDirectory;

    public VaultConfiguration() {
        this.tempDirectory = getJavaTempDir();
    }

    @Builder
    public VaultConfiguration(String ansibleVaultPath, String vaultPasswordFilePath, String tempDirectory) {
        this.ansibleVaultPath = requireNotBlank(ansibleVaultPath);
        this.vaultPasswordFilePath = requireNotBlank(vaultPasswordFilePath);
        this.tempDirectory = isBlank(tempDirectory) ? getJavaTempDir() : tempDirectory;
    }

    private String getJavaTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public VaultConfiguration copyOf() {
        return VaultConfiguration.builder()
                .ansibleVaultPath(ansibleVaultPath)
                .vaultPasswordFilePath(vaultPasswordFilePath)
                .tempDirectory(tempDirectory)
                .build();
    }
}
