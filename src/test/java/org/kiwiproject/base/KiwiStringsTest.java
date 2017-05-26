package org.kiwiproject.base;

import com.google.common.collect.ImmutableList;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;

public class KiwiStringsTest {

    public static final ImmutableList<String> EXPECTED_LIST_FOR_SPLITS = ImmutableList.of("this", "is", "a", "sample", "string");
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    // split tests

    @Test
    public void testSplit_WhenNullArgument() {
        assertThatThrownBy(() -> KiwiStrings.split(null, ','))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSplit_WhenEmptyArgument() {
        assertThat(KiwiStrings.split("", ',').iterator().hasNext()).isFalse();
    }

    @Test
    public void testSplit_WhenBlankArgument() {
        assertThat(KiwiStrings.split("   ", ',').iterator().hasNext()).isFalse();
    }

    @Test
    public void testSplitOnSpaces() {
        softly.assertThat(newArrayList(KiwiStrings.splitOnSpaces("this is a sample string")))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(newArrayList(KiwiStrings.splitOnSpaces("  this    is a    sample    string   ")))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitOnTabs() {
        softly.assertThat(newArrayList(KiwiStrings.splitOnTabs("this\tis\ta\tsample\tstring")))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(newArrayList(KiwiStrings.splitOnTabs("\t  this  \t\t is \t a \t  \t sample \t string  \t ")))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitOnCommas() {
        softly.assertThat(newArrayList(KiwiStrings.splitOnCommas("this,is,a,sample,string")))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(newArrayList(KiwiStrings.splitOnCommas(",, , this,, is, ,, a, sample,, , ,,, string, ,,")))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitOnNewlines() {
        softly.assertThat(newArrayList(KiwiStrings.splitOnNewlines("this\nis\na\nsample\nstring")))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(newArrayList(KiwiStrings.splitOnNewlines("\n\nthis  \n is \n\n a\n \n \n sample    \n string\n \n  ")))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitOnRandomCharacter() {
        softly.assertThat(newArrayList(KiwiStrings.split("this|is|a|sample|string", '|')))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(newArrayList(KiwiStrings.split("||  | this || | is| a | | sample|  string ||  ||", '|')))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitOnStringSeparator() {
        softly.assertThat(newArrayList(KiwiStrings.split("this||is||a||sample||string", "||")))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(newArrayList(KiwiStrings.split("<-><->this<-><->is  <-><-> <-> a <->  <->  sample <-><->  string<->   <->", "<->")))
                .isEqualTo(expectedListForSplits());
    }

    // split to lists tests

    @Test
    public void testSplitToList_WhenNullArgument() {
        assertThatThrownBy(() -> KiwiStrings.splitToList(null, ','))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSplitToList_WhenEmptyArgument() {
        assertThat(KiwiStrings.splitToList("", ',')).isEmpty();
    }

    @Test
    public void testSplitToList_WhenBlankArgument() {
        assertThat(KiwiStrings.splitToList("   ", ',')).isEmpty();
    }

    @Test
    public void testSplitToListOnSpaces() {
        softly.assertThat(KiwiStrings.splitToListOnSpaces("this is a sample string"))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(KiwiStrings.splitToListOnSpaces("  this    is a    sample    string   "))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitToListOnTabs() {
        softly.assertThat(KiwiStrings.splitToListOnTabs("this\tis\ta\tsample\tstring"))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(KiwiStrings.splitToListOnTabs("\t  this  \t\t is \t a \t  \t sample \t string  \t "))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitToListOnCommas() {
        softly.assertThat(KiwiStrings.splitToListOnCommas("this,is,a,sample,string"))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(KiwiStrings.splitToListOnCommas(",, , this,, is, ,, a, sample,, , ,,, string, ,,"))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitToListOnNewlines() {
        softly.assertThat(KiwiStrings.splitToListOnNewlines("this\nis\na\nsample\nstring"))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(KiwiStrings.splitToListOnNewlines("\n\nthis  \n is \n\n a\n \n \n sample    \n string\n \n  "))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitToListOnRandomCharacter() {
        softly.assertThat(KiwiStrings.splitToList("this|is|a|sample|string", '|'))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(KiwiStrings.splitToList("||  | this || | is| a | | sample|  string ||  ||", '|'))
                .isEqualTo(expectedListForSplits());
    }

    @Test
    public void testSplitToListOnStringSeparator() {
        softly.assertThat(KiwiStrings.splitToList("this||is||a||sample||string", "||"))
                .isEqualTo(expectedListForSplits());
        softly.assertThat(KiwiStrings.splitToList("<-><->this<-><->is  <-><-> <-> a <->  <->  sample <-><->  string<->   <->", "<->"))
                .isEqualTo(expectedListForSplits());
    }

    private static List<String> expectedListForSplits() {
        return EXPECTED_LIST_FOR_SPLITS;
    }

    // Guava-style tests

    @Test
    public void testFormatGuavaStyle_WhenNullTemplate() {
        assertThat(KiwiStrings.formatGuavaStyle(null, "arg1", "arg2")).isEqualTo("null [arg1, arg2]");
    }

    @Test
    public void testFormatGuavaStyle_WhenNoPlaceholders() {
        assertThat(KiwiStrings.formatGuavaStyle("This is a simple string")).isEqualTo("This is a simple string");
    }

    @Test
    public void testFormatGuavaStyle_WhenOnePlaceholder() {
        assertThat(KiwiStrings.formatGuavaStyle("This is a string with %s placeholder", 1))
                .isEqualTo("This is a string with 1 placeholder");
    }

    @Test
    public void testFormatGuavaStyle_WhenMultiplePlaceholders() {
        assertThat(KiwiStrings.formatGuavaStyle("This is a %s template with %s %s", "sample", 3, "placeholders"))
                .isEqualTo("This is a sample template with 3 placeholders");
    }

    @Test
    public void testFormatGuavaStyle_WhenMoreValuesThanPlaceholders() {
        assertThat(KiwiStrings.formatGuavaStyle("This template has %s values than placeholders", "more", 42, "foo"))
                .isEqualTo("This template has more values than placeholders [42, foo]");
    }

    @Test
    public void testFormatGuavaStyle_WhenNotEnoughValuesForPlaceholders() {
        assertThat(KiwiStrings.formatGuavaStyle("This %s does %s have enough %s", "template"))
                .isEqualTo("This template does %s have enough %s");
    }

    @Test
    public void testFormatGuavaStyle_WhenNullArguments() {
        assertThat(KiwiStrings.formatGuavaStyle("This %s has %s %s arguments", null, null, null))
                .isEqualTo("This null has null null arguments");
    }

    // SLF4J-style tests

    @Test
    public void testFormatSlf4jStyle_WhenNullTemplate() {
        assertThat(KiwiStrings.formatSlf4jJStyle(null, "arg1", "arg2")).isEqualTo("null [arg1, arg2]");
    }

    @Test
    public void testFormatSlf4jStyle_WhenNoPlaceholders() {
        assertThat(KiwiStrings.formatSlf4jJStyle("This is a simple string")).isEqualTo("This is a simple string");
    }

    @Test
    public void testFormatSlf4jStyle_WhenOnePlaceholder() {
        assertThat(KiwiStrings.formatSlf4jJStyle("This is a string with {} placeholder", 1))
                .isEqualTo("This is a string with 1 placeholder");
    }

    @Test
    public void testFormatSlf4jStyle_WhenMultiplePlaceholders() {
        assertThat(KiwiStrings.formatSlf4jJStyle("This is a {} template with {} {}", "sample", 3, "placeholders"))
                .isEqualTo("This is a sample template with 3 placeholders");
    }

    @Test
    public void testFormatSlf4jStyle_WhenMoreValuesThanPlaceholders() {
        assertThat(KiwiStrings.formatSlf4jJStyle("This template has {} values than placeholders", "more", 42, "foo"))
                .isEqualTo("This template has more values than placeholders [42, foo]");
    }

    @Test
    public void testFormatSlf4jStyle_WhenNotEnoughValuesForPlaceholders() {
        assertThat(KiwiStrings.formatSlf4jJStyle("This {} does {} have enough {}", "template"))
                .isEqualTo("This template does {} have enough {}");
    }

    @Test
    public void testFormatSlf4jStyle_WhenNullArguments() {
        assertThat(KiwiStrings.formatSlf4jJStyle("This {} has {} {} arguments", null, null, null))
                .isEqualTo("This null has null null arguments");
    }

    // Generic format tests

    @Test
    public void testFormat_WhenNullTemplate() {
        assertThat(KiwiStrings.format(null, "arg1", "arg2")).isEqualTo("null [arg1, arg2]");
    }

    @Test
    public void testFormat_WhenNoPlaceholders() {
        assertThat(KiwiStrings.format("This is a simple string")).isEqualTo("This is a simple string");
    }

    @Test
    public void testFormat_WhenNoPlaceholders_AndSomeValues() {
        assertThat(KiwiStrings.format("This is a simple string", 42, "foo", "bar"))
                .isEqualTo("This is a simple string [42, foo, bar]");
    }

    @Test
    public void testFormat_WhenOnePlaceholder_GuavaStyle() {
        assertThat(KiwiStrings.format("This is a string with %s placeholder", 1))
                .isEqualTo("This is a string with 1 placeholder");
    }

    @Test
    public void testFormat_WhenOnePlaceholder_Slf4jStyle() {
        assertThat(KiwiStrings.format("This is a string with {} placeholder", 1))
                .isEqualTo("This is a string with 1 placeholder");
    }

    @Test
    public void testFormat_WhenMultiplePlaceholders_GuavaStyle() {
        assertThat(KiwiStrings.format("This is a %s template with %s %s", "sample", 3, "placeholders"))
                .isEqualTo("This is a sample template with 3 placeholders");
    }

    @Test
    public void testFormat_WhenMultiplePlaceholders_Slf4jStyle() {
        assertThat(KiwiStrings.format("This is a {} template with {} {}", "sample", 3, "placeholders"))
                .isEqualTo("This is a sample template with 3 placeholders");
    }

    @Test
    public void testFormat_WhenMoreValuesThanPlaceholders_Slf4jStyle() {
        assertThat(KiwiStrings.format("This template has {} values than placeholders", "more", 42, "foo"))
                .isEqualTo("This template has more values than placeholders [42, foo]");
    }

    @Test
    public void testFormat_WhenMoreValuesThanPlaceholders_GuavaStyle() {
        assertThat(KiwiStrings.format("This template has %s values than placeholders", "more", 42, "foo"))
                .isEqualTo("This template has more values than placeholders [42, foo]");
    }

    @Test
    public void testFormat_WhenNotEnoughValuesForPlaceholders_GuavaStyle() {
        assertThat(KiwiStrings.format("This %s does %s have enough %s", "template"))
                .isEqualTo("This template does %s have enough %s");
    }

    @Test
    public void testFormat_WhenNotEnoughValuesForPlaceholders_Slf4jStyle() {
        assertThat(KiwiStrings.format("This {} does {} have enough {}", "template"))
                .isEqualTo("This template does {} have enough {}");
    }

    @Test
    public void testFormat_WhenNullArguments_GuavaStyle() {
        assertThat(KiwiStrings.format("This %s has %s %s arguments", null, null, null))
                .isEqualTo("This null has null null arguments");
    }

    @Test
    public void testFormat_WhenNullArguments_Slf4jStyle() {
        assertThat(KiwiStrings.format("This {} has {} {} arguments", null, null, null))
                .isEqualTo("This null has null null arguments");
    }

    @Test
    public void testFormat_WhenSomeoneUsesUnknownReplacement() {
        assertThat(KiwiStrings.format("This __ template uses __ __", "template", "unknown", "replacements"))
                .isEqualTo("This __ template uses __ __ [template, unknown, replacements]");
    }

    // Alias f method sanity tests

    @Test
    public void testFormat_AliasMethod_GuavaStyle() {
        assertThat(KiwiStrings.f("A %s template", "cool")).isEqualTo("A cool template");
    }

    @Test
    public void testFormat_AliasMethod_Slf4jStyle() {
        assertThat(KiwiStrings.f("This {} has {} arguments and one extra value", "awesome template", 2, 42))
                .isEqualTo("This awesome template has 2 arguments and one extra value [42]");
    }

}