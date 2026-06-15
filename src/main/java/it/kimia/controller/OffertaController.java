package it.kimia.controller;

import it.kimia.model.SavedOfferta;
import it.kimia.service.AuthService;
import it.kimia.service.CartService;
import it.kimia.service.OffertaService;
import it.kimia.service.PdfService;
import it.kimia.service.TrasportoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final AuthService auth;
    private final PdfService pdf;
    public OffertaController(OffertaService offerte, CartService cart, TrasportoService trasporto, AuthService auth, PdfService pdf) { this.offerte=offerte; this.cart=cart; this.trasporto=trasporto; this.auth=auth; this.pdf=pdf; }

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
    @PostMapping("/save") public String save(@ModelAttribute OffertaService.OffertaForm form, HttpSession session, RedirectAttributes ra) throws Exception {
        offerte.save(normalizeTrasporto(form), currentUser(session));
        ra.addFlashAttribute("message", "Offerta salvata correttamente.");
        return "redirect:/offerta/storico";
    }
    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> pdf(@ModelAttribute OffertaService.OffertaForm form) throws Exception {
        OffertaService.OffertaForm normalized = normalizeTrasporto(form);
        String html = offerte.previewHtml(normalized);
        byte[] pdfBytes = pdf.renderHtml(html);

        String numero = isBlank(normalized.numero()) ? "offerta" : normalized.numero().replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = "offerta-" + numero + ".pdf";

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(fileName).build().toString())
            .body(pdfBytes);
    }
    @GetMapping("/pdf")
    public String pdfGetFallback(RedirectAttributes ra) {
        ra.addFlashAttribute("message", "Apri il PDF dalla pagina di anteprima offerta.");
        return "redirect:/offerta";
    }
    @GetMapping("/storico") public String storico(Model model, HttpSession session) throws Exception { model.addAttribute("offerte", offerte.all(currentUser(session))); return "offerte-storico"; }
    @GetMapping("/{id:\\d+}") public String open(@PathVariable int id, Model model, HttpSession session) throws Exception {
        SavedOfferta o = offerte.find(id, currentUser(session)).orElseThrow();
        offerte.loadIntoCart(o);
        OffertaService.OffertaForm form = new OffertaService.OffertaForm(o.getNumero(), o.getDataOfferta(), o.getCliente(), o.getCantiere(), o.getRegione() != null ? o.getRegione() : "Umbria", String.valueOf(o.getScadenzaGg()), o.getAgente(), o.getEmail(), o.getTel(), o.getNote(), o.getTrasporto(), false);
        model.addAttribute("html", offerte.previewHtml(form));
        model.addAttribute("form", form);
        model.addAttribute("cartTotal", cart.total());
        return "offerta-preview";
    }
    @PostMapping("/delete/{id:\\d+}") public String delete(@PathVariable int id, HttpSession session) throws Exception { offerte.delete(id, currentUser(session)); return "redirect:/offerta/storico"; }
    @GetMapping("/{id:\\d+}/html") @ResponseBody public String html(@PathVariable int id, HttpSession session) throws Exception {
        SavedOfferta o = offerte.find(id, currentUser(session)).orElseThrow();
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

    private String currentUser(HttpSession session) {
        String username = auth.currentUsername(session);
        if (username == null) throw new IllegalStateException("Utente non autenticato");
        return username;
    }
}
