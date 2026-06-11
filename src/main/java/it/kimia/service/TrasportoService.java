package it.kimia.service;

import it.kimia.db.Database;
import it.kimia.util.Formatter;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class TrasportoService {
    public List<String> regioni() { return List.of("Valle D'Aosta","Piemonte","Lombardia","Trentino-Alto Adige","Veneto","Friuli Venezia Giulia","Emilia-Romagna","Liguria","Toscana","Umbria","Marche","Lazio","Abruzzo","Molise","Campania","Basilicata","Puglia","Calabria","Sicilia","Sardegna","Altro"); }
    public String testo(String regione) throws SQLException {
        if (regione == null || regione.isBlank()) regione = "Umbria";
        Map<String,Object> cfg = new HashMap<>();
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT * FROM transport_region_config WHERE region_name=?")) { ps.setString(1, regione); try(ResultSet rs=ps.executeQuery()){ if(rs.next()){ ResultSetMetaData m=rs.getMetaData(); for(int i=1;i<=m.getColumnCount();i++) cfg.put(m.getColumnName(i),rs.getObject(i)); } } }
        List<Map<String,Object>> rates = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT region_name, weight_band_kg, rate_eur FROM transport_rates WHERE region_name=? ORDER BY weight_band_kg")) { ps.setString(1, regione); try(ResultSet rs=ps.executeQuery()){ while(rs.next()){ Map<String,Object> r=new HashMap<>(); r.put("weight_band_kg",rs.getString("weight_band_kg")); r.put("rate_eur",rs.getDouble("rate_eur")); rates.add(r); } } }
        String[] labels={"   0 ~    3 Kg","   4 ~   10 Kg","  11 ~   30 Kg","  31 ~   50 Kg","  51 ~  150 Kg"," 151 ~  300 Kg"," 301 ~  500 Kg"," 501 ~ 1.000 Kg"};
        boolean isola = "Sicilia".equals(regione)||"Sardegna".equals(regione);
        StringBuilder txt=new StringBuilder();
        if(!isola && cfg.get("direct_load_cost_eur")!=null){ txt.append("[A] Consegna diretta da Perugia con carichi completi (>= 300 ql, circa 20 pedane)\n"); txt.append("    Costo trasporto: Euro ").append(Formatter.fmtN(((Number)cfg.get("direct_load_cost_eur")).doubleValue())).append("\n\n"); }
        if(!isola && cfg.get("bilico_rate_per_kg")!=null){ txt.append("[C] Consegna con Bilico per carichi > 9.000 Kg\n"); txt.append("    Tariffa: Euro ").append(Formatter.fmtQ(((Number)cfg.get("bilico_rate_per_kg")).doubleValue())).append("/Kg + Euro ").append(cfg.getOrDefault("bilico_unload_surcharge",0)).append(" scarico Bilico\n\n"); }
        txt.append("[Corriere] Costi per fascia di peso - Regione: ").append(regione).append("\n");
        for(int i=0;i<rates.size();i++){ double v=((Number)rates.get(i).get("rate_eur")).doubleValue(); txt.append("    ").append(i<labels.length?labels[i]:rates.get(i).get("weight_band_kg")+" Kg").append("  ->  Euro ").append(Formatter.fmtN(v)).append("\n"); }
        txt.append("    oltre 1.000 Kg  ->  contattare ufficio per quotazione\n");
        double surcharge=cfg.get("hydraulic_surcharge_eur") instanceof Number ? ((Number)cfg.get("hydraulic_surcharge_eur")).doubleValue() : 50;
        txt.append("\n    Maggiorazione consegna con sponda idraulica: Euro ").append(String.format("%.0f", surcharge)).append(",00\n");
        if(cfg.get("extra_notes")!=null) txt.append("\n").append(cfg.get("extra_notes")).append("\n");
        return txt.toString();
    }
}
