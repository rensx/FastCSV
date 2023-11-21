package de.siegmar.fastcsv.writer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

class FastBufferedWriterTest {

    private final StringWriter sw = new StringWriter();
    private final CsvWriter.FastBufferedWriter cw = new CsvWriter.FastBufferedWriter(sw, 8192);

    @Test
    void appendSingle() throws IOException {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8192; i++) {
            sb.append("ab");
            cw.write('a');
            cw.write('b');
        }
        cw.close();

        assertThat(sb).asString().isEqualTo(sw.toString());
    }

    @Test
    void appendArray() throws IOException {
        final StringBuilder sb = new StringBuilder();

        final String strValue = "ab";
        final char[] caValue = strValue.toCharArray();
        for (int i = 0; i < 8192; i++) {
            sb.append(strValue);
            cw.write(caValue, 0, caValue.length);
        }
        cw.close();

        assertThat(sb).asString().isEqualTo(sw.toString());
    }

    @Test
    void appendString() throws IOException {
        final StringBuilder sb = new StringBuilder();

        final String strValue = "ab";
        for (int i = 0; i < 8192; i++) {
            sb.append(strValue);
            cw.write(strValue, 0, 2);
        }
        cw.close();

        assertThat(sb).asString().isEqualTo(sw.toString());
    }

    @Test
    void appendLarge() throws IOException {
        final String sb = buildLargeData();
        cw.write(sb, 0, sb.length());

        assertThat(sw).asString().isEqualTo(sb);
    }

    private String buildLargeData() {
        return "ab".repeat(8192);
    }

}
