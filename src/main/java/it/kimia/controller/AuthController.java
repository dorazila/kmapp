package it.kimia.controller;

import it.kimia.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String next, Model model) {
        model.addAttribute("next", safeNext(next));
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String next,
            HttpSession session,
            Model model) throws Exception {
        if (auth.login(username, password, session)) {
            return "redirect:" + safeNext(next);
        }
        model.addAttribute("next", safeNext(next));
        model.addAttribute("error", "Credenziali non valide.");
        model.addAttribute("username", username);
        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        auth.logout(session);
        return "redirect:/login";
    }

    private static String safeNext(String next) {
        if (next == null || next.isBlank()) return "/";
        String trimmed = next.trim();
        return trimmed.startsWith("/") && !trimmed.startsWith("//") ? trimmed : "/";
    }
}
