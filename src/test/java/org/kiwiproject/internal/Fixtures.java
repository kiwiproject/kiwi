package org.kiwiproject.internal;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// TODO This came in the ansible.vault.testing package (#272) but I moved to internal until figure out what to do with
//  it. The Dropwizard FixtureHelpers in dropwizard-testing has a fixture method that returns a String but it does not
//  have a direct analog for the fixturePath method. Currently this is in the test sources so it won't be part of an
//  actual kiwi JAR.
public class Fixtures {

    public static String fixture(String resourceName) {
        try {
            @SuppressWarnings("UnstableApiUsage") var url = Resources.getResource(resourceName);
            var path = Paths.get(url.toURI());
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Error reading fixture: " + resourceName, e);
        }
    }

    public static Path fixturePath(String resourceName) {
        @SuppressWarnings("UnstableApiUsage") var url = Resources.getResource(resourceName);
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error getting path of fixture: " + resourceName, e);
        }
    }
}
