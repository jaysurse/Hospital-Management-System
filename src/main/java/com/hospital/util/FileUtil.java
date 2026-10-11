package com.hospital.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * File utility handling report exports to CSV and formatted plain-text files.
 * Demonstrates Java File I/O, Character Streams, and the Iterator design pattern.
 */
public final class FileUtil {

    private FileUtil() {
    }

    /**
     * Exports tabular data to a standard RFC 4180-compliant CSV file.
     * Demonstrates the Iterator Pattern for traversing rows and columns.
     */
    public static void exportToCSV(File targetFile, List<String> headers, List<List<String>> dataRows) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("Target file cannot be null");
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8)))) {
            // Write CSV Header using Iterator
            if (headers != null && !headers.isEmpty()) {
                Iterator<String> headerIt = headers.iterator();
                while (headerIt.hasNext()) {
                    writer.print(escapeCsvValue(headerIt.next()));
                    if (headerIt.hasNext()) {
                        writer.print(",");
                    }
                }
                writer.println();
            }

            // Write Data Rows using Nested Iterators (Iterator Pattern)
            if (dataRows != null) {
                Iterator<List<String>> rowIt = dataRows.iterator();
                while (rowIt.hasNext()) {
                    List<String> row = rowIt.next();
                    Iterator<String> colIt = row.iterator();
                    while (colIt.hasNext()) {
                        writer.print(escapeCsvValue(colIt.next()));
                        if (colIt.hasNext()) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
            }
            writer.flush();
        }
    }

    /**
     * Exports a clinical report or invoice to a clean, formatted plain text file.
     */
    public static void exportToTextFile(File targetFile, String title, List<String> sections) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("Target file cannot be null");
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8)))) {
            writer.println("==========================================================================");
            writer.println("  " + title.toUpperCase());
            writer.println("  Generated on: " + DateUtil.formatTimestamp(DateUtil.toSqlTimestamp(LocalDateTime.now())));
            writer.println("==========================================================================");
            writer.println();

            if (sections != null) {
                for (String section : sections) {
                    writer.println(section);
                }
            }

            writer.println();
            writer.println("--------------------------------------------------------------------------");
            writer.println("  End of Official Hospital Document - Confidential");
            writer.println("--------------------------------------------------------------------------");
            writer.flush();
        }
    }

    private static String escapeCsvValue(String value) {
        if (value == null) return "\"\"";
        String s = value.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
