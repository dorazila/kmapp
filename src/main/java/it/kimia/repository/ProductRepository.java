package it.kimia.repository;

import it.kimia.db.Database;
import it.kimia.model.Product;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class ProductRepository {
    public static class SystemOption {
        private final String code;
        private final String name;

        public SystemOption(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
    }

    public List<Product> findAll(String category, String q, String listinoType, String system) throws SQLException {
        List<Product> out = new ArrayList<>();
        boolean hasSystem = system != null && !system.isBlank();
        StringBuilder sql = new StringBuilder("SELECT p.product_code, p.product_name, p.description, p.unit_measure, p.category_name, p.listino_price, p.discount_rate, p.net_price, p.listino_type, lp.suggested_qty FROM products p");
        if (hasSystem) {
            sql.append(" JOIN (SELECT product_code, SUM(quantita) AS suggested_qty FROM lavorazione_prodotti WHERE codice_lavorazione=? GROUP BY product_code) lp ON lp.product_code = p.product_code");
        } else {
            sql.append(" LEFT JOIN (SELECT NULL AS product_code, NULL AS suggested_qty) lp ON 1=0");
        }
        sql.append(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (hasSystem) {
            args.add(system);
        }
        if (category != null && !category.isBlank() && !"Tutti i prodotti".equals(category)) { sql.append(" AND p.category_name=?"); args.add(category); }
        if (listinoType != null && !listinoType.isBlank() && !"Tutti".equals(listinoType)) { sql.append(" AND p.listino_type=?"); args.add(listinoType); }
        if (q != null && !q.isBlank()) { sql.append(" AND (lower(p.product_name) LIKE ? OR lower(p.product_code) LIKE ? OR lower(p.description) LIKE ?)"); String s="%"+q.toLowerCase()+"%"; args.add(s); args.add(s); args.add(s); }
        sql.append(" ORDER BY p.category_name, p.product_name, p.product_code");
        try (PreparedStatement ps = Database.get().prepareStatement(sql.toString())) {
            for (int i=0;i<args.size();i++) ps.setObject(i+1,args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public Optional<Product> findByCode(String code) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT product_code, product_name, description, unit_measure, category_name, listino_price, discount_rate, net_price, listino_type FROM products WHERE product_code=? LIMIT 1")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        }
    }

    public List<String> categories() throws SQLException {
        List<String> out = new ArrayList<>();
        out.add("Tutti i prodotti");
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT category_name FROM product_categories ORDER BY sort_order"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(rs.getString(1));
        }
        return out;
    }

    public List<SystemOption> systems() throws SQLException {
        List<SystemOption> out = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement("""
                SELECT l.codice_lavorazione, l.nome_lavorazione
                FROM lavorazioni l
                WHERE EXISTS (
                    SELECT 1
                    FROM lavorazione_prodotti lp
                    JOIN products p ON p.product_code = lp.product_code
                    WHERE lp.codice_lavorazione = l.codice_lavorazione
                )
                ORDER BY l.nome_lavorazione, l.codice_lavorazione
            """); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new SystemOption(rs.getString(1), rs.getString(2)));
            }
        }
        return out;
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product(rs.getString("product_code"), rs.getString("product_name"), rs.getString("description"), rs.getString("unit_measure"), rs.getString("category_name"), rs.getDouble("listino_price"), rs.getDouble("discount_rate"), rs.getDouble("net_price"));
        p.setListinoType(rs.getString("listino_type"));
        Object suggestedQty = rs.getObject("suggested_qty");
        p.setSuggestedQty(suggestedQty != null ? rs.getDouble("suggested_qty") : null);
        return p;
    }
}
