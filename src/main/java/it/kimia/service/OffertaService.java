package it.kimia.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.kimia.model.CartItem;
import it.kimia.model.SavedOfferta;
import it.kimia.repository.OffertaRepository;
import it.kimia.util.HtmlBuilder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Service
public class OffertaService {
    private final OffertaRepository repo;
    private final CartService cart;
    private final ObjectMapper mapper = new ObjectMapper();
    public OffertaService(OffertaRepository repo, CartService cart) { this.repo = repo; this.cart = cart; }

    public String previewHtml(OffertaForm f) {
        return HtmlBuilder.buildOffertaHtml(f.numero(), nvl(f.dataOfferta(), LocalDate.now().toString()), f.cliente(), f.cantiere(), f.regione(), nvl(f.scadenzaGg(), "30"), cart.total(), new ArrayList<>(cart.items()), f.note(), f.trasporto(), f.agente(), f.tel(), f.email(), f.shortMode());
    }
    public void save(OffertaForm f) throws Exception {
        SavedOfferta o = new SavedOfferta();
        o.setNumero(f.numero()); o.setDataOfferta(nvl(f.dataOfferta(), LocalDate.now().toString())); o.setCliente(f.cliente()); o.setCantiere(f.cantiere()); o.setAgente(f.agente()); o.setEmail(f.email()); o.setTel(f.tel()); o.setRegione(f.regione());
        try { o.setScadenzaGg(Integer.parseInt(nvl(f.scadenzaGg(),"30"))); } catch(Exception e) { o.setScadenzaGg(30); }
        o.setNote(f.note()); o.setTrasporto(f.trasporto()); o.setItemsJson(mapper.writeValueAsString(cart.items())); o.setTotale(cart.total()); repo.save(o);
    }
    public List<SavedOfferta> all() throws SQLException { return repo.findAll(); }
    public Optional<SavedOfferta> find(int id) throws SQLException { return repo.findById(id); }
    public void delete(int id) throws SQLException { repo.delete(id); }
    public void loadIntoCart(SavedOfferta o) throws Exception { cart.clear(); if(o.getItemsJson()==null || o.getItemsJson().isBlank()) return; List<CartItem> items=mapper.readValue(o.getItemsJson(), new TypeReference<>(){}); for(CartItem i:items){ cart.put(i); } }
    private static String nvl(String s, String def){ return s==null||s.isBlank()?def:s; }
    public record OffertaForm(String numero, String dataOfferta, String cliente, String cantiere, String regione, String scadenzaGg, String agente, String email, String tel, String note, String trasporto, boolean shortMode) {}
}
