package com.rental.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// FileService — Central file I/O helper (Member 06 / all members)
// Every repository uses this to read and write text files.
//
// IMPORTANT: Every BufferedReader and BufferedWriter is closed
// immediately after use (inside try-with-resources) to prevent
// the "File Lock" error between reads and writes.
// ============================================================
@Service
public class FileService {

    // The folder where all .txt files are stored (set in application.properties)
    @Value("${app.data.dir:data}")
    private String dataDir;

    // Build the full file path for a given filename (e.g. "users.txt")
    private String getPath(String filename) {
        return dataDir + File.separator + filename;
    }

    // Make sure the data folder and the file exist before we try to read/write
    private void ensureFileExists(String filename) {
        try {
            Path dir  = Paths.get(dataDir);
            Path file = Paths.get(getPath(filename));

            if (!Files.exists(dir)) {
                Files.createDirectories(dir); // create the "data/" folder
            }
            if (!Files.exists(file)) {
                Files.createFile(file);       // create the empty .txt file
            }
        } catch (IOException e) {
            System.err.println("Could not create file: " + filename + " — " + e.getMessage());
        }
    }

    // ---- READ: return every non-empty line as a list of strings ----
    public List<String> readLines(String filename) {
        ensureFileExists(filename);
        List<String> lines = new ArrayList<>();

        // try-with-resources ensures the reader is ALWAYS closed after use
        try (BufferedReader reader = new BufferedReader(new FileReader(getPath(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) { // skip blank lines
                    lines.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + filename + ": " + e.getMessage());
        }

        return lines;
    }

    // ---- WRITE: overwrite the entire file with a new list of lines ----
    // Used for updates and deletes (rewrite all records except the changed one)
    public void writeLines(String filename, List<String> lines) {
        ensureFileExists(filename);

        // try-with-resources ensures the writer is ALWAYS closed after use
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getPath(filename), false))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing " + filename + ": " + e.getMessage());
        }
    }

    // ---- APPEND: add one new line at the end of the file ----
    // Used for INSERT operations — faster than rewriting the whole file
    public void appendLine(String filename, String line) {
        ensureFileExists(filename);

        // append=true means we add to the end, not overwrite
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getPath(filename), true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error appending to " + filename + ": " + e.getMessage());
        }
    }
}
