package de.siegmar.fastcsv.reader;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents an immutable CSV record with named (and indexed) fields.
 * <p>
 * The field values are never {@code null}. Empty fields are represented as empty strings.
 * <p>
 * Named CSV records are created by {@link NamedCsvReader}.
 *
 * @see NamedCsvReader
 * @see CsvReader
 */
@SuppressWarnings("PMD.ArrayIsStoredDirectly")
public final class NamedCsvRecord extends CsvRecord {

    private final String[] header;

    NamedCsvRecord(final String[] header, final CsvRecord csvRecord) {
        super(csvRecord);
        this.header = header;
    }

    /**
     * Retrieves the value of a field by its case-sensitive name, considering the first occurrence in case of
     * duplicates.
     * <p>
     * This method is equivalent to {@code findField(name).orElseThrow(NoSuchElementException::new)} although a
     * more explanatory exception message is provided.
     *
     * @param name case-sensitive name of the field to be retrieved
     * @return field value, never {@code null}
     * @throws NoSuchElementException if this record has no such field
     * @throws NullPointerException   if name is {@code null}
     * @see #findField(String)
     * @see #findFields(String)
     */
    public String getField(final String name) {
        final int fieldIdx = findHeaderIndex(name);

        // Check if the field index is valid
        if (fieldIdx == -1) {
            throw new NoSuchElementException(MessageFormat.format(
                "Header does not contain a field ''{0}''. Valid names are: {1}", name, Arrays.toString(header)));
        }
        if (fieldIdx >= fields.length) {
            throw new NoSuchElementException(MessageFormat.format(
                "Field ''{0}'' is on index {1}, but current record only contains {2} fields",
                name, fieldIdx + 1, fields.length));
        }

        // Return the value of the field
        return fields[fieldIdx];
    }

    // Finds the index of the first occurrence of the given header name (case-sensitive); returns -1 if not found
    private int findHeaderIndex(final String name) {
        for (int i = 0; i < header.length; i++) {
            if (name.equals(header[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retrieves the value of a field by its case-sensitive name, considering the first occurrence in case of
     * duplicates.
     * <p>
     * This method is equivalent to {@code findFields(name).stream().findFirst()} but more performant.
     *
     * @param name case-sensitive name of the field to be retrieved
     * @return An {@code Optional} containing the value of the field if found,
     *     or an empty {@code Optional} if the field is not present. Never returns {@code null}.
     * @throws NullPointerException if name is {@code null}
     * @see #findFields(String)
     */
    public Optional<String> findField(final String name) {
        final int fieldIdx = findHeaderIndex(name);

        // Check if the field index is valid
        if (fieldIdx == -1 || fieldIdx >= fields.length) {
            return Optional.empty();
        }

        // Return the value of the field wrapped in an Optional
        return Optional.of(fields[fieldIdx]);
    }

    /**
     * Collects all field values with the given name (case-sensitive) in the order they appear in the header.
     *
     * @param name case-sensitive name of the field to collect values for
     * @return the field values (empty list if record doesn't contain that field), never {@code null}
     * @throws NullPointerException if name is {@code null}
     */
    public List<String> findFields(final String name) {
        final int bound = header.length;
        final List<String> ret = new ArrayList<>(bound);
        for (int i = 0; i < bound; i++) {
            if (name.equals(header[i])) {
                ret.add(fields[i]);
            }
        }
        return ret;
    }

    /**
     * Constructs an ordered map, associating header names with corresponding field values of this record,
     * considering the first occurrence in case of duplicates.
     * <p>
     * The constructed map will only contain entries for fields that have a key and a value. No map entry will have a
     * {@code null} key or value.
     * <p>
     * If you need to collect all fields with the same name (duplicate header), use {@link #getFieldsAsMapList()}.
     *
     * @return an ordered map of header names and field values of this record, never {@code null}
     * @see #getFieldsAsMapList()
     */
    public Map<String, String> getFieldsAsMap() {
        final int bound = commonSize();
        final Map<String, String> map = new LinkedHashMap<>(bound);
        for (int i = 0; i < bound; i++) {
            map.putIfAbsent(header[i], fields[i]);
        }
        return map;
    }

    /**
     * Constructs an unordered map, associating header names with an ordered list of corresponding field values of
     * this record.
     * <p>
     * The constructed map will only contain entries for fields that have a key and a value. No map entry will have a
     * {@code null} key or value.
     * <p>
     * If you don't have to handle duplicate headers, you may simply use {@link #getFieldsAsMap()}.
     *
     * @return an unordered map of header names and field values of this record, never {@code null}
     * @see #getFieldsAsMap()
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Map<String, List<String>> getFieldsAsMapList() {
        final int bound = commonSize();
        final Map<String, List<String>> map = new HashMap<>(bound);
        for (int i = 0; i < bound; i++) {
            final String key = header[i];
            List<String> val = map.get(key);
            if (val == null) {
                val = new LinkedList<>();
                map.put(key, val);
            }
            val.add(fields[i]);
        }
        return map;
    }

    // Mappings will only be created for fields that have a key and a value – return the minimum of both sizes
    private int commonSize() {
        return Math.min(header.length, fields.length);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NamedCsvRecord.class.getSimpleName() + "[", "]")
            .add("startingLineNumber=" + startingLineNumber)
            .add("fields=" + Arrays.toString(fields))
            .add("comment=" + comment)
            .add("header=" + Arrays.toString(header))
            .toString();
    }

}
