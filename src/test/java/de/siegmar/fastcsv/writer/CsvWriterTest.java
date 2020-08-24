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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;

public class CsvWriterTest {

    private final CsvWriterBuilder csvWriter = CsvWriter.builder().lineDelimiter("\n");

    @Test
    public void nullDelimit() throws IOException {
        assertEquals("foo,,bar\n", write("foo", null, "bar"));
        assertEquals("foo,,bar\n", write("foo", "", "bar"));
        assertEquals("foo,\",\",bar\n", write("foo", ",", "bar"));
    }

    @Test
    public void emptyDelimit() throws IOException {
        csvWriter.textDelimitStrategy(TextDelimitStrategy.EMPTY);
        assertEquals("foo,,bar\n", write("foo", null, "bar"));
        assertEquals("foo,\"\",bar\n", write("foo", "", "bar"));
        assertEquals("foo,\",\",bar\n", write("foo", ",", "bar"));
    }

    @Test
    public void oneLineSingleValue() throws IOException {
        assertEquals("foo\n", write("foo"));
    }

    @Test
    public void oneLineTwoValues() throws IOException {
        assertEquals("foo,bar\n", write("foo", "bar"));
    }

    @Test
    public void twoLinesSingleValue() throws IOException {
        final Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[] {"foo"});
        rows.add(new String[] {"bar"});

        assertEquals("foo\nbar\n", write(rows));
    }

    @Test
    public void twoLinesTwoValues() throws IOException {
        assertEquals("foo,bar\n", write("foo", "bar"));
    }

    @Test
    public void delimitText() throws IOException {
        assertEquals("a,\"b,c\",\"d\ne\",\"f\"\"g\",,\n",
            write("a", "b,c", "d\ne", "f\"g", "", null));
    }

    @Test
    public void alwaysDelimitText() throws IOException {
        csvWriter.textDelimitStrategy(TextDelimitStrategy.ALWAYS);
        assertEquals("\"a\",\"b,c\",\"d\ne\",\"f\"\"g\",\"\",\"\"\n",
            write("a", "b,c", "d\ne", "f\"g", "", null));
    }

    @Test
    public void fieldSeparator() throws IOException {
        csvWriter.fieldSeparator(';');
        assertEquals("foo;bar\n", write("foo", "bar"));
    }

    @Test
    public void textDelimiter() throws IOException {
        csvWriter.textDelimiter('\'');
        assertEquals("'foo,bar'\n", write("foo,bar"));
    }

    @Test
    public void escape() throws IOException {
        assertEquals("foo,\"\"\"bar\"\"\"\n", write("foo", "\"bar\""));
    }

    @Test
    public void appending() throws IOException {
        final StringWriter sw = new StringWriter();
        final CsvWriter appender = csvWriter.build(sw);
        appender.writeField("foo");
        appender.writeField("bar");
        assertEquals("foo,bar", sw.toString());
    }

    private String write(final String... cols) throws IOException {
        final Collection<String[]> rows = new ArrayList<>();
        rows.add(cols);

        return write(rows);
    }

    private String write(final Collection<String[]> rows) throws IOException {
        final StringWriter sw = new StringWriter();
        final CsvWriter to = csvWriter.build(sw);
        for (String[] row : rows) {
            to.writeLine(row);
        }
        return sw.toString();
    }

}
