
package com.kodnest.auth.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import com.kodnest.auth.Entity.User;
import com.kodnest.auth.Service.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // Display registration page
    @GetMapping("/")
    public String showRegisterPage() {
        return "index";
    }

    // Register a new user
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute User user,
            Model model) {

        try {
            userService.register(user);

            model.addAttribute(
                    "message",
                    "Registration successful! Please log in.");

            return "login";

        } catch (Exception e) {
            model.addAttribute(
                    "error",
                    "Registration failed. Please try again.");

            return "index";
        }
    }

    // Display login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Display home page only for a verified user
    @GetMapping("/home")
    public String showHomePage(
            HttpSession session,
            Model model) {

        String email =
                (String) session.getAttribute("loggedInUser");

        if (email == null) {
            return "redirect:/login";
        }

        model.addAttribute("email", email);

        return "home";
    }

    // Authenticate user and send OTP
    @PostMapping("/login")
    public String loginUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model) {

        boolean result =
                userService.loginAndGenerateOTP(email, password);

        if (result) {
            model.addAttribute("email", email);
            return "verifyotp";
        }

        model.addAttribute(
                "error",
                "Invalid email or password.");

        return "login";
    }

    // Verify OTP and create a logged-in session
    @PostMapping("/verifyotp")
    public String verifyOtp(
            @RequestParam("email") String email,
            @RequestParam("otp") String otp,
            Model model,
            HttpSession session) {

        boolean result =
                userService.verifyOtp(email, otp);

        if (result) {
            session.setAttribute("loggedInUser", email);

            return "redirect:/home";
        }

        model.addAttribute("email", email);

        model.addAttribute(
                "error",
                "Invalid or expired OTP. Please try again or log in again.");

        return "verifyotp";
    }
    
 // Log out and invalidate the session
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
