package com.rental.service;

import com.rental.model.Role;
import com.rental.model.User;
import com.rental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ============================================================
// UserService — Business logic for login and registration.
// Member 02: Secure login, registration, and role identification
//            (Admin vs Customer).
// ============================================================
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Member 02 — Register a new user
    public User registerUser(User user) {
        // Check if email already exists (no duplicate accounts)
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        // Give a UUID as the user's ID
        user.setId(UUID.randomUUID().toString());

        // The FIRST user ever registered becomes the ADMIN automatically
        if (userRepository.count() == 0) {
            user.setRole(Role.ADMIN);
        } else if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER); // all others are customers
        }

        userRepository.save(user);
        return user;
    }

    // Member 02 — Login: check email + password, return user or null
    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt.get(); // login success
        }
        return null; // login failed — wrong credentials
    }

    // Get all users (Admin only — Member 06 dashboard)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get a single user by UUID
    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    // Delete user by UUID (Admin only)
    public void deleteUser(String id) {
        userRepository.delete(id);
    }

    // Update a user's name and/or password from the profile page
    public void updateUser(User updatedUser) {
        User existing = getUserById(updatedUser.getId());
        if (existing != null) {
            existing.setName(updatedUser.getName());
            // Only update password if the user typed a new one
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existing.setPassword(updatedUser.getPassword());
            }
            userRepository.save(existing);
        }
    }
}
