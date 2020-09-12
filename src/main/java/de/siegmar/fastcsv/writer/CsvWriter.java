/*
 * Copyright 2015 Oliver Siegmar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.siegmar.fastcsv.writer;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * This is the main class for writing CSV data.
 *
 * Example use:
 * <pre>{@code
 * try (CloseableCsvWriter csv = CsvWriter.builder().build(file, StandardCharsets.UTF_8)) {
 *     csv.writeLine("Hello", "world");
 * }
 * }</pre>
 *
 * @author Oliver Siegmar
 */
public class CsvWriter implements Closeable {

    private static final char CR = '\r';
    private static final char LF = '\n';

    private final Writer writer;
    private final char fieldSeparator;
    private final char quoteCharacter;
    private final QuoteStrategy quoteStrategy;
    private final String lineDelimiter;

    private boolean isNewline = true;

    CsvWriter(final Writer writer, final char fieldSeparator, final char quoteCharacter,
              final QuoteStrategy quoteStrategy, final LineDelimiter lineDelimiter) {
        this.writer = writer;
        this.fieldSeparator = fieldSeparator;
        this.quoteCharacter = quoteCharacter;
        this.quoteStrategy = Objects.requireNonNull(quoteStrategy);
        this.lineDelimiter = Objects.requireNonNull(lineDelimiter).toString();
    }

    /**
     * Creates a {@link CsvWriterBuilder} instance used to configure and create instances of
     * this class.
     * @return CsvWriterBuilder instance with default settings.
     */
    public static CsvWriterBuilder builder() {
        return new CsvWriterBuilder();
    }

    /**
     * Appends a field to the current row. Automatically adds field separator and quotes as
     * required.
     *
     * @param value the field to append (can be {@code null})
     * @throws IOException if a write error occurs
     * @return This CsvWriter.
     */
    public CsvWriter writeField(final String value) throws IOException {
        if (!isNewline) {
            write(fieldSeparator);
        } else {
            isNewline = false;
        }

        if (value == null) {
            if (quoteStrategy == QuoteStrategy.ALWAYS) {
                write(quoteCharacter);
                write(quoteCharacter);
            }
            return this;
        }

        if (value.isEmpty()) {
            if (quoteStrategy == QuoteStrategy.ALWAYS
                || quoteStrategy == QuoteStrategy.EMPTY) {
                write(quoteCharacter);
                write(quoteCharacter);
            }
            return this;
        }

        final int length = value.length();
        boolean needsQuotes = quoteStrategy == QuoteStrategy.ALWAYS;
        int nextDelimPos = -1;

        for (int i = 0; i < length; i++) {
            final char c = value.charAt(i);
            if (c == quoteCharacter) {
                needsQuotes = true;
                nextDelimPos = i;
                break;
            }
            if (!needsQuotes && (c == fieldSeparator || c == LF || c == CR)) {
                needsQuotes = true;
            }
        }

        if (needsQuotes) {
            write(quoteCharacter);
        }

        if (nextDelimPos > -1) {
            writeEscaped(value, length, nextDelimPos);
        } else {
            write(value, 0, length);
        }

        if (needsQuotes) {
            write(quoteCharacter);
        }

        return this;
    }

    @SuppressWarnings({"checkstyle:FinalParameters", "checkstyle:ParameterAssignment"})
    private void writeEscaped(final String value, final int length, int nextDelimPos)
        throws IOException {

        int startPos = 0;
        do {
            final int len = nextDelimPos - startPos + 1;
            write(value, startPos, len);
            write(quoteCharacter);
            startPos += len;

            nextDelimPos = -1;
            for (int i = startPos; i < length; i++) {
                if (value.charAt(i) == quoteCharacter) {
                    nextDelimPos = i;
                    break;
                }
            }
        } while (nextDelimPos > -1);

        if (length > startPos) {
            write(value, startPos, length - startPos);
        }
    }

    /**
     * Appends a complete line - one or more fields and new line character(s) at the end.
     *
     * @param values the fields to append ({@code null} values are handled as empty strings, if
     *               not configured otherwise ({@link QuoteStrategy#EMPTY}))
     * @throws IOException if a write error occurs
     * @return This CsvWriter.
     */
    public CsvWriter writeLine(final Iterable<String> values) throws IOException {
        for (final String value : values) {
            writeField(value);
        }
        endLine();
        return this;
    }

    /**
     * Appends a complete line - one or more fields and new line character(s) at the end.
     *
     * @param values the fields to append ({@code null} values are handled as empty strings, if
     *               not configured otherwise ({@link QuoteStrategy#EMPTY}))
     * @throws IOException if a write error occurs
     * @return This CsvWriter.
     */
    public CsvWriter writeLine(final String... values) throws IOException {
        for (final String value : values) {
            writeField(value);
        }
        endLine();
        return this;
    }

    /**
     * Appends new line character(s) to the current line.
     *
     * @throws IOException if a write error occurs
     * @return This CsvWriter.
     */
    public CsvWriter endLine() throws IOException {
        write(lineDelimiter, 0, lineDelimiter.length());
        isNewline = true;
        return this;
    }

    private void write(final String value, final int off, final int length)
        throws IOException {

        writer.write(value, off, length);
    }

    private void write(final char c) throws IOException {
        writer.write(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        writer.close();
    }

}
