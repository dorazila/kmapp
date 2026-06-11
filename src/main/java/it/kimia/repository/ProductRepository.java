package it.kimia.repository;

import it.kimia.db.Database;
import it.kimia.model.Product;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class ProductRepository {
    public List<Product> findAll(String category, String q) throws SQLException {
        List<Product> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT product_code, product_name, description, unit_measure, category_name, listino_price, discount_rate, net_price FROM products WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (category != null && !category.isBlank() && !"Tutti i prodotti".equals(category)) { sql.append(" AND category_name=?"); args.add(category); }
        if (q != null && !q.isBlank()) { sql.append(" AND (lower(product_name) LIKE ? OR lower(product_code) LIKE ? OR lower(description) LIKE ?)"); String s="%"+q.toLowerCase()+"%"; args.add(s); args.add(s); args.add(s); }
        sql.append(" ORDER BY category_name, product_name, product_code");
        try (PreparedStatement ps = Database.get().prepareStatement(sql.toString())) {
            for (int i=0;i<args.size();i++) ps.setObject(i+1,args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public Optional<Product> findByCode(String code) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT product_code, product_name, description, unit_measure, category_name, listino_price, discount_rate, net_price FROM products WHERE product_code=? LIMIT 1")) {
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

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product(rs.getString("product_code"), rs.getString("product_name"), rs.getString("description"), rs.getString("unit_measure"), rs.getString("category_name"), rs.getDouble("listino_price"), rs.getDouble("discount_rate"), rs.getDouble("net_price"));
        p.setListinoType("NLIS");
        return p;
    }
}
