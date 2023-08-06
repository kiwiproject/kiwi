package org.kiwiproject.jsch;

import io.dropwizard.core.Configuration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class AppConfiguration extends Configuration {

    @NotNull
    @Valid
    private SftpConfig sftpConfig;
}
