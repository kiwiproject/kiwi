package org.kiwiproject.jsch;

import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
class AppConfiguration extends Configuration {

    @NotNull
    @Valid
    private SftpConfig sftpConfig;
}
