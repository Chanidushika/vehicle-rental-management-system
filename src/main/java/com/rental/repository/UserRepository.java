package com.rental.repository;

import com.rental.model.Role;
import com.rental.model.User;
import com.rental.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ============================================================
// UserRepository — File-based CRUD for users.txt
// CSV format per line:
//   id,name,email,password,role
// ============================================================
@Repository
public class UserRepository {

    private static final String FILENAME = "users.txt";

    @Autowired
    private FileService fileService;

    // ---- READ ALL users ----
    public List<User> findAll() {
        List<String> lines = fileService.readLines(FILENAME);
        List<User> users = new ArrayList<>();

        for (String line : lines) {
            User u = User.fromCsv(line);
            if (u != null) users.add(u);
        }
        return users;
    }

    // ---- READ by UUID ----
    public Optional<User> findById(String id) {
        return findAll().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    // ---- READ by email (used for login check — Member 02) ----
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    // ---- Count total users (used to set first user as ADMIN) ----
    public long count() {
        return fileService.readLines(FILENAME).size();
    }

    // ---- SAVE (INSERT or UPDATE) ----
    public void save(User user) {
        List<String> lines = fileService.readLines(FILENAME);
        String newLine = user.toCsv();
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {
            String existingId = lines.get(i).split(",")[0];
            if (existingId.equals(user.getId())) {
                lines.set(i, newLine); // UPDATE
                found = true;
                break;
            }
        }

        if (!found) {
            fileService.appendLine(FILENAME, newLine); // INSERT
        } else {
            fileService.writeLines(FILENAME, lines);   // UPDATE — rewrite file
        }
    }

    // ---- DELETE by ID ----
    public void delete(String id) {
        List<String> lines = fileService.readLines(FILENAME);
        lines.removeIf(line -> line.split(",")[0].equals(id));
        fileService.writeLines(FILENAME, lines);
    }
}
