package com.rental.controller;

import com.rental.model.Role;
import com.rental.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// ============================================================
// HomeController — Handles the root "/" URL.
// After login, redirects Admin to dashboard and Customer to vehicles.
// ============================================================
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // If user is logged in, send them to the right dashboard
        if (loggedInUser != null) {
            if (loggedInUser.getRole() == Role.ADMIN) {
                return "redirect:/admin/dashboard"; // Admin sees the admin dashboard
            } else {
                return "redirect:/vehicles";        // Customer sees the vehicles list
            }
        }

        return "index"; // Not logged in → show the home landing page
    }
}
