package org.kiwiproject.regex;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is a {@link java.util.Spliterator} that lets you traverse {@link MatchResult}s of a {@link Matcher}.
 * Generally you should use one of the {@code stream} methods, but if you need a {@link java.util.Spliterator}
 * instance you can use the constructor directly.
 * <p>
 * Gratefully included here in Kiwi courtesy of Philipp Wagner from his "A Spliterator for MatchResults in Java"
 * blog entry. Slightly modified to check arguments and add an instance {@code stream} method.
 * <p>
 * Original blog post: https://bytefish.de/blog/matcher_spliterator/
 */
public class MatchSpliterator extends Spliterators.AbstractSpliterator<MatchResult> {

    private static final boolean PARALLEL_FALSE = false;

    private final Matcher matcher;

    /**
     * Create a new instance from the given {@link Matcher}. Usually you won't use this directly. Instead use the
     * {@code stream} methods.
     *
     * @param matcher the Matcher to split
     */
    public MatchSpliterator(Matcher matcher) {
        super(Long.MAX_VALUE, NONNULL | ORDERED);

        this.matcher = requireNotNull(matcher);
    }

    @Override
    public boolean tryAdvance(Consumer<? super MatchResult> action) {
        if (matcher.find()) {
            action.accept(matcher.toMatchResult());
            return true;
        }

        return false;
    }

    /**
     * Create a  {@link Stream} of {@link MatchResult} from this instance.
     *
     * @return a stream of match results
     */
    public Stream<MatchResult> stream() {
        return stream(matcher);
    }

    /**
     * Create a {@link Stream} of {@link MatchResult} using the given regular expression and String input.
     * <p>
     * <em>This method will compile a new {@link Pattern} every time it is called, even if the same regular
     * expression is supplied.</em> Compiling regular expressions is considered an "expensive" operation.
     * Therefore you should generally prefer {@link #stream(Pattern, String)} since you can precompile a
     * {@link Pattern} and re-use it many times.
     *
     * @param regex the regular expression as a String
     * @param input the input to match against
     * @return a stream of match results
     * @throws IllegalArgumentException if the regex is blank
     */
    public static Stream<MatchResult> stream(String regex, String input) {
        checkArgumentNotBlank(regex);

        return stream(Pattern.compile(regex), input);
    }

    /**
     * Create a {@link Stream} of {@link MatchResult} using the given {@link Pattern} and String input.
     *
     * @param pattern the {@link Pattern} to use when matching the input
     * @param input   the input to match against
     * @return a stream of match results
     * @throws IllegalArgumentException if the pattern is null
     */
    public static Stream<MatchResult> stream(Pattern pattern, String input) {
        checkArgumentNotNull(pattern);

        return stream(pattern.matcher(input));
    }

    /**
     * Create a {@link Stream} of {@link MatchResult} using the given {@link Matcher}
     *
     * @param matcher the {@link Matcher} to use
     * @return a stream of match results
     * @throws IllegalArgumentException if the matcher is null
     */
    public static Stream<MatchResult> stream(Matcher matcher) {
        checkArgumentNotNull(matcher);

        return StreamSupport.stream(new MatchSpliterator(matcher), PARALLEL_FALSE);
    }
}
