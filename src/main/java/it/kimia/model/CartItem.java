package it.kimia.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem {
    private String product_code;
    private String product_name;
    private String description;
    private String category_name;
    private String unit_measure;
    private double net_price;
    private double listino_price;
    private Double qty;
    private Double sconto; // 0.0 - 0.75 (fraction), null = no discount

    public CartItem() {}

    public CartItem(Product p) {
        this.product_code  = p.getProductCode();
        this.product_name  = p.getProductName();
        this.description   = p.getDescription();
        this.category_name = p.getCategoryName();
        this.unit_measure  = p.getUnitMeasure();
        this.net_price     = p.getNetPrice();
        this.listino_price = p.getListinoPrice();
        this.qty           = null;
        this.sconto        = null;
    }

    /** Effective price after discount */
    public double getEffectivePrice() {
        return sconto != null ? net_price * (1.0 - sconto) : net_price;
    }

    /** Row total (null if qty not set) */
    public Double getRowTotal() {
        if (qty == null || qty == 0) return null;
        return getEffectivePrice() * qty;
    }

    public String getProduct_code()   { return product_code; }
    public String getProduct_name()   { return product_name; }
    public String getDescription()    { return description; }
    public String getCategory_name()  { return category_name; }
    public String getUnit_measure()   { return unit_measure; }
    public double getNet_price()      { return net_price; }
    public double getListino_price()  { return listino_price; }
    public Double getQty()            { return qty; }
    public Double getSconto()         { return sconto; }

    public void setProduct_code(String v)  { this.product_code = v; }
    public void setProduct_name(String v)  { this.product_name = v; }
    public void setDescription(String v)   { this.description = v; }
    public void setCategory_name(String v) { this.category_name = v; }
    public void setUnit_measure(String v)  { this.unit_measure = v; }
    public void setNet_price(double v)     { this.net_price = v; }
    public void setListino_price(double v) { this.listino_price = v; }
    public void setQty(Double v)           { this.qty = v; }
    public void setSconto(Double v)        { this.sconto = v; }
}
