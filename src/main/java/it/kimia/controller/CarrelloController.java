package it.kimia.controller;

import it.kimia.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/carrello")
public class CarrelloController {
    private final CartService cart;
    public CarrelloController(CartService cart) { this.cart=cart; }
    @GetMapping public String view(Model model) { model.addAttribute("items", cart.items()); model.addAttribute("cart", cart); model.addAttribute("total", cart.total()); return "carrello"; }
    @PostMapping("/update") public String update(@RequestParam String key, @RequestParam(required=false) Double qty, @RequestParam(required=false) Double sconto) { cart.update(key, qty, sconto); return "redirect:/carrello"; }
    @PostMapping("/remove") public String remove(@RequestParam String key) { cart.remove(key); return "redirect:/carrello"; }
    @PostMapping("/clear") public String clear() { cart.clear(); return "redirect:/carrello"; }
}
