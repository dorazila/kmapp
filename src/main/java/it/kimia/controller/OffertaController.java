package it.kimia.controller;

import it.kimia.model.SavedOfferta;
import it.kimia.service.CartService;
import it.kimia.service.OffertaService;
import it.kimia.service.TrasportoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/offerta")
public class OffertaController {
    private final OffertaService offerte;
    private final CartService cart;
    private final TrasportoService trasporto;
    public OffertaController(OffertaService offerte, CartService cart, TrasportoService trasporto) { this.offerte=offerte; this.cart=cart; this.trasporto=trasporto; }

    @GetMapping public String form(Model model) throws Exception {
        model.addAttribute("regions", trasporto.regioni());
        model.addAttribute("today", LocalDate.now().toString());
        model.addAttribute("defaultTrasporto", trasporto.testo("Umbria"));
        model.addAttribute("cartTotal", cart.total());
        return "offerta";
    }
    @PostMapping("/preview") public String preview(@ModelAttribute OffertaService.OffertaForm form, Model model) throws Exception {
        OffertaService.OffertaForm normalized = normalizeTrasporto(form);
        model.addAttribute("html", offerte.previewHtml(normalized));
        model.addAttribute("form", normalized);
        model.addAttribute("cartTotal", cart.total());
        return "offerta-preview";
    }
    @PostMapping("/save") public String save(@ModelAttribute OffertaService.OffertaForm form, RedirectAttributes ra) throws Exception {
        offerte.save(normalizeTrasporto(form));
        ra.addFlashAttribute("message", "Offerta salvata correttamente.");
        return "redirect:/offerta/storico";
    }
    @GetMapping("/storico") public String storico(Model model) throws Exception { model.addAttribute("offerte", offerte.all()); return "offerte-storico"; }
    @GetMapping("/{id}") public String open(@PathVariable int id, Model model) throws Exception {
        SavedOfferta o = offerte.find(id).orElseThrow();
        offerte.loadIntoCart(o);
        OffertaService.OffertaForm form = new OffertaService.OffertaForm(o.getNumero(), o.getDataOfferta(), o.getCliente(), o.getCantiere(), o.getRegione() != null ? o.getRegione() : "Umbria", String.valueOf(o.getScadenzaGg()), o.getAgente(), o.getEmail(), o.getTel(), o.getNote(), o.getTrasporto(), false);
        model.addAttribute("html", offerte.previewHtml(form));
        model.addAttribute("form", form);
        model.addAttribute("cartTotal", cart.total());
        return "offerta-preview";
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable int id) throws Exception { offerte.delete(id); return "redirect:/offerta/storico"; }
    @GetMapping("/{id}/html") @ResponseBody public String html(@PathVariable int id) throws Exception {
        SavedOfferta o = offerte.find(id).orElseThrow();
        return o.getItemsJson();
    }

    @GetMapping("/trasporto")
    @ResponseBody
    public String trasportoByRegione(@RequestParam(required = false) String regione) throws Exception {
        return trasporto.testo(isBlank(regione) ? "Umbria" : regione);
    }

    private OffertaService.OffertaForm normalizeTrasporto(OffertaService.OffertaForm form) throws Exception {
        String selectedRegion = isBlank(form.regione()) ? "Umbria" : form.regione();
        String resolved = resolveTrasportoText(selectedRegion, form.trasporto());
        return new OffertaService.OffertaForm(
            form.numero(),
            form.dataOfferta(),
            form.cliente(),
            form.cantiere(),
            selectedRegion,
            form.scadenzaGg(),
            form.agente(),
            form.email(),
            form.tel(),
            form.note(),
            resolved,
            form.shortMode()
        );
    }

    private String resolveTrasportoText(String selectedRegion, String currentText) throws Exception {
        if (isBlank(currentText)) return trasporto.testo(selectedRegion);

        String normalizedCurrent = normalize(currentText);
        for (String region : trasporto.regioni()) {
            if (normalizedCurrent.equals(normalize(trasporto.testo(region)))) {
                return trasporto.testo(selectedRegion);
            }
        }
        return currentText;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replace("\r\n", "\n").trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
