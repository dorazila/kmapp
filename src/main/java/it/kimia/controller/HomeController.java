package it.kimia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    @GetMapping("/") public String home() { return "index"; }

    @GetMapping("/documentazione")
    public String documentazione(@RequestParam(name = "sezione", defaultValue = "compositi") String sezione, Model model) {
        String selected = switch (sezione) {
            case "moderna" -> "moderna";
            case "restauro" -> "restauro";
            default -> "compositi";
        };

        String sourceUrl = switch (selected) {
            case "moderna" -> "https://www.kimia.it/recupero-edilizia-moderna";
            case "restauro" -> "https://www.kimia.it/restauro-edilizia-storico-monumentale";
            default -> "https://www.kimia.it/rinforzi-con-sistemi-compositi";
        };

        model.addAttribute("docSection", selected);
        model.addAttribute("docUrl", sourceUrl);
        return "documentazione";
    }
}
