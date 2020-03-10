package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;
import static org.kiwiproject.base.KiwiStrings.COMMA;
import static org.kiwiproject.base.KiwiStrings.NEWLINE;
import static org.kiwiproject.base.KiwiStrings.SPACE;
import static org.kiwiproject.base.KiwiStrings.TAB;
import static org.kiwiproject.base.KiwiStrings.blankToNull;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.base.KiwiStrings.format;
import static org.kiwiproject.base.KiwiStrings.splitOnCommas;
import static org.kiwiproject.base.KiwiStrings.splitToList;
import static org.kiwiproject.base.KiwiStrings.splitWithTrimAndOmitEmpty;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.util.BlankStringArgumentsProvider;

import java.util.List;

@DisplayName("KiwiStrings")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiStringsTest {

    @Nested
    class Split {

        @Nested
        class WithTrimAndOmitEmpty {

            @Test
            void shouldThrow_WithNullArgument() {
                assertThatThrownBy(() -> splitWithTrimAndOmitEmpty(null))
                        .isExactlyInstanceOf(NullPointerException.class);
            }

            @Test
            void shouldReturnEmptyList_WithEmptyArgument() {
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty(""));
                assertThat(strings).isEmpty();
            }

            @Test
            void shouldReturnEmptyList_WithBlankArgument() {
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty("  "));
                assertThat(strings).isEmpty();
            }

            @Test
            void shouldSplitOnSpaces_WithNoExplicitSeparatorArg() {
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty(" this   is a   string  "));
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnSpaces_WithCharSequenceArgument() {
                StringBuilder builder = new StringBuilder()
                        .append("  this      ")
                        .append("  is    ")
                        .append(" a")
                        .append("      string  ");
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty(builder));
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnSpaces() {
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty(" this   is a   string  ", SPACE));
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnCommas() {
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty(",, , ,  this ,,  is, , ,a ,  ,string , ,, ,", COMMA));
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnTabs() {
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty("\t\t\t this \t\t   is\t \t   \ta \t  \tstring \t \t\t \t", TAB));
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnNewLines() {
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty("\n\n\n this \n\n   is\n \n   \na \n  \nstring \n \n\n \n", NEWLINE));
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnPipe() {
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty("||| this ||   is| |   |a |  |string | || |", '|'));
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnStringSeparator() {
                List<String> strings = newArrayList(splitWithTrimAndOmitEmpty("this, is, , , a, string", ", "));
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }
        }

        @Nested
        class ToList {

            @Test
            void shouldSplitOnSpaces_WithNoExplicitSeparatorArg() {
                List<String> strings = splitToList(" this   is a   string  ");
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnSpace() {
                List<String> strings = splitToList(" this   is a   string  ", SPACE);
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnCommas() {
                List<String> strings = splitToList(",, , ,  this ,,  is, , ,a ,  ,string , ,, ,", COMMA);
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnTabs() {
                List<String> strings = splitToList("\t\t\t this \t\t   is\t \t   \ta \t  \tstring \t \t\t \t", TAB);
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnNewLines() {
                List<String> strings = newArrayList(splitToList("\n\n\n this \n\n   is\n \n   \na \n  \nstring \n \n\n \n", NEWLINE));
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnPipe() {
                List<String> strings = splitToList("||| this ||   is| |   |a |  |string | || |", '|');
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitOnStringSeparator() {
                List<String> strings = splitToList("this, is, , , a, string", ", ");
                assertThat(strings).hasSize(4).containsAll(expectedListForSplits());
            }

            @Test
            void shouldSplitWithMaxGroups() {
                List<String> expectedList = newArrayList("this", "is  a    string");

                List<String> strings = splitToList(" this   is  a    string  ", SPACE, 2);
                assertThat(strings).hasSize(2).containsAll(expectedList);
            }

            @Test
            void shouldSplitWithMaxGroups_WithStringSeparator() {
                List<String> expectedList = newArrayList("this", "is||a|| string");

                List<String> strings = splitToList(" this||is||a|| string  ", "||", 2);
                assertThat(strings).hasSize(2).containsAll(expectedList);
            }
        }

        @Nested
        class OnCommas {

            @Test
            void shouldThrow_WithNullArgument() {
                assertThatThrownBy(() -> splitOnCommas(null))
                        .isExactlyInstanceOf(NullPointerException.class);
            }

            @Test
            void shouldReturnEmptyList_WithEmptyArgument() {
                List<String> strings = newArrayList(splitOnCommas(""));
                assertThat(strings).isEmpty();
            }

            @Test
            void shouldReturnEmptyList_WithBlankArgument() {
                List<String> strings = newArrayList(splitOnCommas("  "));
                assertThat(strings).isEmpty();
            }
        }

        private List<String> expectedListForSplits() {
            return newArrayList("this", "is", "a", "string");
        }
    }

    @Nested
    class Format {

        @Nested
        class GuavaStyle {

            @Test
            void shouldFormat() {
                String result = format("This is a %s template with %s placeholders", "great", 2);
                assertThat(result).isEqualTo("This is a great template with 2 placeholders");
            }

            @Test
            void shouldFormat_ThroughAliasMethod() {
                String result = f("This is a %s template with %s placeholders", "great", 2);
                assertThat(result).isEqualTo("This is a great template with 2 placeholders");
            }

            @Test
            @SuppressWarnings("ConstantConditions")
            void shouldFormat_withSomeArguments_ContainingNullValues() {
                String templateAdjective = null;
                String numPlaceholders = null;
                String result = format("This is a %s template with %s placeholders and all null arguments",
                        templateAdjective, numPlaceholders);
                assertThat(result).isEqualTo("This is a null template with null placeholders and all null arguments");
            }

            @Test
            void shouldFormat_withMoreArgs_ThanPlaceholders() {
                Object o = new Object();
                String result = format("This is a %s template with %s placeholders but more arguments", "great", 2, 3.14, o);
                assertThat(result).isEqualTo("This is a great template with 2 placeholders but more arguments [3.14, " + o.toString() + "]");
            }
        }

        @Nested
        class Slf4jStyle {

            @Test
            void shouldFormat() {
                String result = format("This is a {} template with {} placeholders", "great", 2);
                assertThat(result).isEqualTo("This is a great template with 2 placeholders");
            }

            @Test
            void shouldFormat_ThroughAliasMethod() {
                String result = f("This is a {} template with {} placeholders", "great", 2);
                assertThat(result).isEqualTo("This is a great template with 2 placeholders");
            }

            @Test
            @SuppressWarnings("ConstantConditions")
            void shouldFormat_withSomeArguments_ContainingNullValues() {
                String templateAdjective = null;
                String numPlaceholders = null;
                String result = format("This is a {} template with {} placeholders and all null arguments",
                        templateAdjective, numPlaceholders);
                assertThat(result).isEqualTo("This is a null template with null placeholders and all null arguments");
            }

            @Test
            void shouldFormat_withMoreArgs_ThanPlaceholders() {
                Object o = new Object();
                String result = format("This is a {} template with {} placeholders but more arguments", "great", 2, 3.14, o);
                assertThat(result).isEqualTo("This is a great template with 2 placeholders but more arguments [3.14, " + o.toString() + "]");
            }
        }

        @Test
        void withNullTemplate_ShouldFormat() {
            String result = format(null, "great", 2);
            assertThat(result).isEqualTo("null [great, 2]");
        }

        @Test
        void whenSomeoneIsBeingAJerkAndTriesToMixAndMatchGuavaAndSlf4jPlaceholders() {
            String result = format("This is a %s template with {} parameters, which %s {} properly or as {}",
                    "silly", 5, "does not", "work", "expected");
            assertThat(result).isEqualTo("This is a silly template with {} parameters, which 5 {} properly or as {} [does not, work, expected]");
        }
    }

    @Nested
    class BlankToNull {

        @ParameterizedTest
        @ArgumentsSource(BlankStringArgumentsProvider.class)
        void shouldBeNull_WithBlankStrings(String argument) {
            String result = blankToNull(argument);
            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = { "a ", " a", " a "})
        void shouldReturnValues_WithNonWhitespaceCharacters(String argument) {
            String result = blankToNull(argument);
            assertThat(result).isEqualTo(argument);
        }
    }
}
