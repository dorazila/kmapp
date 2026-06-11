package it.kimia.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalisiComponent {
    private int component_order;
    private String sheet_name;
    private String voce_code;
    private String component_type;
    private String description;
    private String unit_measure;
    private double quantity;
    private double unit_price;
    private double total_price;
    private Double qty_override;
    private Double price_override;

    public AnalisiComponent() {}

    public double getEffectiveQty()   { return qty_override != null   ? qty_override   : quantity; }
    public double getEffectivePrice() { return price_override != null ? price_override : unit_price; }
    public double getTotal()          { return getEffectiveQty() * getEffectivePrice(); }

    public int    getComponent_order()  { return component_order; }
    public String getSheet_name()       { return sheet_name; }
    public String getVoce_code()        { return voce_code; }
    public String getComponent_type()   { return component_type; }
    public String getDescription()      { return description; }
    public String getUnit_measure()     { return unit_measure; }
    public double getQuantity()         { return quantity; }
    public double getUnit_price()       { return unit_price; }
    public double getTotal_price()      { return total_price; }
    public Double getQty_override()     { return qty_override; }
    public Double getPrice_override()   { return price_override; }

    public void setComponent_order(int v)    { this.component_order = v; }
    public void setSheet_name(String v)      { this.sheet_name = v; }
    public void setVoce_code(String v)       { this.voce_code = v; }
    public void setComponent_type(String v)  { this.component_type = v; }
    public void setDescription(String v)     { this.description = v; }
    public void setUnit_measure(String v)    { this.unit_measure = v; }
    public void setQuantity(double v)        { this.quantity = v; }
    public void setUnit_price(double v)      { this.unit_price = v; }
    public void setTotal_price(double v)     { this.total_price = v; }
    public void setQty_override(Double v)    { this.qty_override = v; }
    public void setPrice_override(Double v)  { this.price_override = v; }
}
