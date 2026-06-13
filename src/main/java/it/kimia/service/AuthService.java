package it.kimia.service;

import it.kimia.model.AppUser;
import it.kimia.repository.UserRepository;
import it.kimia.util.Passwords;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {
    public static final String SESSION_USER = "currentUser";

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public boolean login(String username, String password, HttpSession session) throws SQLException {
        String normalized = normalize(username);
        if (normalized == null || !isAllowedUsername(normalized)) return false;
        Optional<AppUser> user = users.findByUsername(normalized);
        if (user.isEmpty() || !Passwords.verify(password, user.get().getPasswordHash())) return false;
        session.setAttribute(SESSION_USER, user.get().getUsername());
        return true;
    }

    public String currentUsername(HttpSession session) {
        Object value = session != null ? session.getAttribute(SESSION_USER) : null;
        return value instanceof String username && !username.isBlank() ? username : null;
    }

    public void logout(HttpSession session) {
        if (session != null) session.invalidate();
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isAllowedUsername(String username) {
        String normalized = username.toLowerCase(Locale.ROOT);
        return "admin".equals(normalized) || normalized.endsWith("@kimia.it");
    }
}
