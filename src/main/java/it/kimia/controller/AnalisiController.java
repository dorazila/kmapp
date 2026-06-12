package it.kimia.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.kimia.model.AnalisiComponent;
import it.kimia.repository.AnalisiRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ap")
public class AnalisiController {
    private final AnalisiRepository repo;
    private final ObjectMapper mapper;
    public AnalisiController(AnalisiRepository repo, ObjectMapper mapper) { this.repo = repo; this.mapper = mapper; }

    @GetMapping
    public String index(@RequestParam(required = false) String voce, Model model) throws Exception {
        var sections = repo.sections();
        if ((voce == null || voce.isBlank()) && !sections.isEmpty() && !sections.values().iterator().next().isEmpty())
            voce = sections.values().iterator().next().get(0);
        List<AnalisiComponent> components = voce == null ? List.of() : repo.components(voce);
        model.addAttribute("sections", sections);
        model.addAttribute("selectedVoce", voce);
        model.addAttribute("selectedSheet", components.isEmpty() ? "" : components.get(0).getSheet_name());
        model.addAttribute("introDescription", voce == null ? "" : repo.introDescription(voce));
        model.addAttribute("components", components);
        model.addAttribute("total", components.stream().mapToDouble(AnalisiComponent::getTotal).sum());
        return "analisi";
    }

    @GetMapping("/storico/{id}")
    public String openSaved(@PathVariable int id, Model model) throws Exception {
        var saved = repo.findSavedById(id);
        List<Map<String, Object>> rows = mapper.readValue(saved.getSnapshotJson(), new TypeReference<>() {});
        List<AnalisiComponent> components = rows.stream().map(row -> {
            AnalisiComponent c = new AnalisiComponent();
            c.setComponent_order(((Number) row.get("order")).intValue());
            c.setComponent_type((String) row.get("type"));
            c.setDescription((String) row.get("description"));
            String um = (String) row.getOrDefault("unitMeasure", "");
            c.setUnit_measure(um != null ? um : "");
            double qty   = row.get("qty")   != null ? ((Number) row.get("qty")).doubleValue()   : 0;
            double price = row.get("price") != null ? ((Number) row.get("price")).doubleValue() : 0;
            c.setQuantity(qty);
            c.setUnit_price(price);
            c.setTotal_price(qty * price);
            c.setSheet_name(saved.getSheetName());
            c.setVoce_code(saved.getVoceCode());
            return c;
        }).toList();
        model.addAttribute("sections", repo.sections());
        model.addAttribute("selectedVoce", saved.getVoceCode());
        model.addAttribute("selectedSheet", saved.getSheetName());
        model.addAttribute("introDescription", repo.introDescription(saved.getVoceCode()));
        model.addAttribute("components", components);
        model.addAttribute("total", saved.getTotale() != null ? saved.getTotale() : 0.0);
        model.addAttribute("savedAnalisi", saved);
        return "analisi";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> save(@RequestBody Map<String, Object> body) throws Exception {
        String titolo       = (String) body.get("titolo");
        String voceCode     = (String) body.get("voceCode");
        String sheetName    = (String) body.get("sheetName");
        String snapshotJson = (String) body.get("snapshotJson");
        Double totale = body.get("totale") != null ? ((Number) body.get("totale")).doubleValue() : null;
        repo.saveAnalisi(titolo, voceCode, sheetName, snapshotJson, totale);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/storico")
    public String storico(Model model) throws Exception {
        model.addAttribute("analisi", repo.saved());
        return "analisi-storico";
    }
}
