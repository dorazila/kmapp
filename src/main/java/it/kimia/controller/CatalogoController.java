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
    @GetMapping public String list(@RequestParam(required=false) String category, @RequestParam(required=false) String q, @RequestParam(required=false) String listinoType, @RequestParam(required=false) String system, @RequestParam(required=false) String added, Model model) throws Exception {
        Set<String> selectedCodes = cart.items().stream().map(i -> i.getProduct_code()).collect(Collectors.toSet());

        model.addAttribute("products", products.findAll(category, q, listinoType, system));
        model.addAttribute("categories", products.categories());
        model.addAttribute("systems", products.systems());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedListinoType", listinoType);
        model.addAttribute("selectedSystem", system);
        model.addAttribute("q", q);
        model.addAttribute("addedCode", added);
        model.addAttribute("selectedCodes", selectedCodes);
        model.addAttribute("cartCount", cart.items().size());
        return "catalogo";
    }
    @PostMapping("/add/{code}") public String add(@PathVariable String code, @RequestParam(required=false) String category, @RequestParam(required=false) String q, @RequestParam(required=false) String listinoType, @RequestParam(required=false) String system) throws Exception {
        return addInternal(code, category, q, listinoType, system);
    }

    @GetMapping("/add/{code}") public String addFromGet(@PathVariable String code, @RequestParam(required=false) String category, @RequestParam(required=false) String q, @RequestParam(required=false) String listinoType, @RequestParam(required=false) String system) throws Exception {
        return addInternal(code, category, q, listinoType, system);
    }

    private String addInternal(String code, String category, String q, String listinoType, String system) throws Exception {
        cart.add(code);
        return "redirect:" + ServletUriComponentsBuilder.fromPath("/catalogo")
            .queryParamIfPresent("category", java.util.Optional.ofNullable(category).filter(s -> !s.isBlank()))
            .queryParamIfPresent("system", java.util.Optional.ofNullable(system).filter(s -> !s.isBlank()))
            .queryParamIfPresent("listinoType", java.util.Optional.ofNullable(listinoType).filter(s -> !s.isBlank()))
            .queryParamIfPresent("q", java.util.Optional.ofNullable(q).filter(s -> !s.isBlank()))
            .queryParam("added", code)
            .build()
            .toUriString();
    }
}
