package it.kimia.service;

import it.kimia.model.CartItem;
import it.kimia.model.Product;
import it.kimia.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.sql.SQLException;
import java.util.*;

@Service
@SessionScope
public class CartService {
    private final ProductRepository products;
    private final Map<String, CartItem> cart = new LinkedHashMap<>();
    public CartService(ProductRepository products) { this.products = products; }
    public Collection<CartItem> items() { return cart.values(); }
    public void add(String code) throws SQLException { Optional<Product> p = products.findByCode(code); p.ifPresent(product -> cart.put(key(product), new CartItem(product))); }
    public void remove(String key) { cart.remove(key); }
    public void put(CartItem item) { cart.put(key(item), item); }
    public void clear() { cart.clear(); }
    public void update(String key, Double qty, Double scontoPct) { CartItem item = cart.get(key); if (item == null) return; item.setQty(qty != null && qty > 0 ? qty : null); if (scontoPct == null) item.setSconto(null); else item.setSconto(Math.min(75, Math.max(0, scontoPct)) / 100.0); }
    public double total() { return cart.values().stream().mapToDouble(i -> i.getRowTotal() == null ? 0 : i.getRowTotal()).sum(); }
    public String key(CartItem p) { return key(p.getProduct_code(), p.getProduct_name(), p.getUnit_measure()); }
    private String key(Product p) { return key(p.getProductCode(), p.getProductName(), p.getUnitMeasure()); }
    private String key(String code, String name, String um) { return nvl(code).trim().toUpperCase()+"|"+nvl(name).trim().toUpperCase()+"|"+nvl(um).trim().toUpperCase(); }
    private static String nvl(String s) { return s == null ? "" : s; }
}
