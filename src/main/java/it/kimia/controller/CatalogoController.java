package it.kimia.controller;

import it.kimia.repository.ProductRepository;
import it.kimia.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {
    private final ProductRepository products; private final CartService cart;
    public CatalogoController(ProductRepository products, CartService cart) { this.products=products; this.cart=cart; }
    @GetMapping public String list(@RequestParam(required=false) String category, @RequestParam(required=false) String q, @RequestParam(required=false) String added, Model model) throws Exception { Set<String> selectedCodes = cart.items().stream().map(i -> i.getProduct_code()).collect(Collectors.toSet()); model.addAttribute("products", products.findAll(category,q)); model.addAttribute("categories", products.categories()); model.addAttribute("selectedCategory", category); model.addAttribute("q", q); model.addAttribute("addedCode", added); model.addAttribute("selectedCodes", selectedCodes); model.addAttribute("cartCount", cart.items().size()); return "catalogo"; }
    @PostMapping("/add/{code}") public String add(@PathVariable String code, @RequestParam(required=false) String category, @RequestParam(required=false) String q) throws Exception {
        cart.add(code);
        return "redirect:" + ServletUriComponentsBuilder.fromPath("/catalogo")
            .queryParamIfPresent("category", java.util.Optional.ofNullable(category).filter(s -> !s.isBlank()))
            .queryParamIfPresent("q", java.util.Optional.ofNullable(q).filter(s -> !s.isBlank()))
            .queryParam("added", code)
            .build()
            .toUriString();
    }
}
