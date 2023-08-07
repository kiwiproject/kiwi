package org.kiwiproject.json;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Options for {@link PropertyMaskingSafePropertyWriter} and {@link KiwiJacksonSerializers}.
 */
@Builder
@Getter
public class PropertyMaskingOptions {

    private static final String DEFAULT_MASK_TEXT_REPLACEMENT = "********";
    private static final String DEFAULT_FAILED_TEXT_REPLACEMENT = "(unable to serialize field)";

    /**
     * Regular expressions that define that field names to be masked, e.g. {@code .*password.*} (note the comparisons
     * will be case-insensitive). Must not be {@code null}, but can be empty. Default is empty.
     */
    @NonNull
    @Builder.Default
    List<String> maskedFieldRegexps = new ArrayList<>();

    /**
     * The replacement text for masked field values. Can be {@code null}.
     */
    @Builder.Default
    String maskedFieldReplacementText = DEFAULT_MASK_TEXT_REPLACEMENT;

    /**
     * The replacement text for serialization errors. Can be {@code null}.
     */
    @Builder.Default
    String serializationErrorReplacementText = DEFAULT_FAILED_TEXT_REPLACEMENT;
}
