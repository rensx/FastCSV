package example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import de.siegmar.fastcsv.reader.CloseableIterator;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;

/**
 * Example for reading CSV data from a file.
 */
@SuppressWarnings("RedundantExplicitVariableType")
public class ExampleCsvReaderWithFileInput {

    private static final String DATA = "foo,bar\nfoo2,bar2";

    public static void main(final String[] args) throws IOException {
        final Path tmpFile = prepareTestFile();

        System.out.println("Reading data via for-each loop:");
        try (CsvReader csv = CsvReader.builder().build(tmpFile)) {
            for (final CsvRecord csvRecord : csv) {
                System.out.println(csvRecord.getFields());
            }
        }

        System.out.println("Reading data via forEach lambda:");
        try (CsvReader csv = CsvReader.builder().build(tmpFile)) {
            csv.forEach(System.out::println);
        }

        System.out.println("Reading data via stream:");
        try (Stream<CsvRecord> stream = CsvReader.builder().build(tmpFile).stream()) {
            stream
                .map(rec -> rec.getField(1))
                .forEach(System.out::println);
        }

        System.out.println("Reading data via iterator:");
        try (CloseableIterator<CsvRecord> iterator = CsvReader.builder().build(tmpFile).iterator()) {
            while (iterator.hasNext()) {
                final CsvRecord csvRecord = iterator.next();
                System.out.println(csvRecord.getFields());
            }
        }
    }

    private static Path prepareTestFile() throws IOException {
        final Path tmpFile = Files.createTempFile("fastcsv", ".csv");
        tmpFile.toFile().deleteOnExit();
        Files.writeString(tmpFile, DATA, StandardCharsets.UTF_8);
        return tmpFile;
    }

}
