package com.rental.controller;

import com.rental.model.Role;
import com.rental.model.User;
import com.rental.service.AdminDashboardService;
import com.rental.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// ============================================================
// AdminController — Admin-only pages: dashboard and user management.
// Member 06: Dashboard shows revenue, bookings, and vehicle stats.
// ============================================================
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminDashboardService dashboardService;

    @Autowired
    private UserService userService;

    // Member 06 — Admin dashboard with full platform statistics
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        // Load stats from the service (reads from files)
        model.addAttribute("stats", dashboardService.getDashboardStats());
        return "admin/dashboard";
    }

    // List all registered users (Admin only)
    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    // Delete a user by their UUID (Admin only)
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == Role.ADMIN) {
            userService.deleteUser(id);
        }
        return "redirect:/admin/users";
    }
}
