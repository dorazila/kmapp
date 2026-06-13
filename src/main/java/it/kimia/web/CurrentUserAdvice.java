package it.kimia.web;

import it.kimia.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {
    private final AuthService auth;

    public CurrentUserAdvice(AuthService auth) {
        this.auth = auth;
    }

    @ModelAttribute("currentUser")
    public String currentUser(HttpSession session) {
        return auth.currentUsername(session);
    }
}
