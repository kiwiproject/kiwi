package org.kiwiproject.util.regex;

import static java.util.Objects.requireNonNull;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

/**
 * A simple {@link java.util.Spliterator} over a {@link Matcher}.
 */
public class MatcherSpliterator extends Spliterators.AbstractSpliterator<MatchResult> {

    private final Matcher matcher;

    public MatcherSpliterator(Matcher matcher) {
        super(Long.MAX_VALUE, ORDERED | NONNULL | IMMUTABLE);
        this.matcher = matcher;
    }

    @Override
    public boolean tryAdvance(Consumer<? super MatchResult> action) {
        requireNonNull(action, "action cannot be null");

        if (!matcher.find()) {
            return false;
        }

        action.accept(matcher.toMatchResult());
        return true;
    }
}
