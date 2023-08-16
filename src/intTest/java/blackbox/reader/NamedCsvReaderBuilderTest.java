package blackbox.reader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static testutil.NamedCsvRowAssert.NAMED_CSV_ROW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import testutil.NamedCsvRowAssert;

@SuppressWarnings("PMD.CloseResource")
class NamedCsvReaderBuilderTest {

    private static final String DATA = "header1,header2\nfoo,bar\n";
    private static final Map<String, String> EXPECTED = Map.of(
        "header1", "foo",
        "header2", "bar");

    private final NamedCsvReader.NamedCsvReaderBuilder crb = NamedCsvReader.builder();

    @Test
    void nullInput() {
        assertThatThrownBy(() -> crb.build((String) null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void fieldSeparator() {
        assertThat(crb.fieldSeparator(';').build("h1;h2\nfoo,bar;baz").stream())
            .singleElement(NAMED_CSV_ROW)
            .fields()
            .containsExactly(entry("h1", "foo,bar"), entry("h2", "baz"));
    }

    @Test
    void quoteCharacter() {
        assertThat(crb.quoteCharacter('_').build("h1,h2\n_foo \", __ bar_,foo \" bar").stream())
            .singleElement(NAMED_CSV_ROW)
            .fields()
            .containsExactly(entry("h1", "foo \", _ bar"), entry("h2", "foo \" bar"));
    }

    @Test
    void commentSkip() {
        assertThat(crb.commentCharacter(';').skipComments(true).build("h1\n#foo\n;bar\nbaz").stream())
            .satisfiesExactly(
                item1 -> NamedCsvRowAssert.assertThat(item1).fields().containsExactly(entry("h1", "#foo")),
                item2 -> NamedCsvRowAssert.assertThat(item2).fields().containsExactly(entry("h1", "baz"))
            );
    }

    @Test
    void builderToString() {
        assertThat(crb).asString()
            .isEqualTo("NamedCsvReaderBuilder[fieldSeparator=,, quoteCharacter=\", "
                + "commentCharacter=#, skipComments=false]");
    }

    @Test
    void string() {
        assertThat(crb.build(DATA).stream())
            .singleElement(NAMED_CSV_ROW)
            .fields()
            .containsExactlyInAnyOrderEntriesOf(EXPECTED);
    }

    @Test
    void path(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("fastcsv.csv");
        Files.write(file, DATA.getBytes(UTF_8));

        try (Stream<NamedCsvRow> stream = crb.build(file).stream()) {
            assertThat(stream)
                .singleElement(NAMED_CSV_ROW)
                .fields()
                .containsExactlyInAnyOrderEntriesOf(EXPECTED);
        }
    }

    @Test
    void chained() {
        final NamedCsvReader reader = NamedCsvReader.builder()
            .fieldSeparator(',')
            .quoteCharacter('"')
            .commentCharacter('#')
            .skipComments(false)
            .build("foo");

        assertThat(reader).isNotNull();
    }

}
