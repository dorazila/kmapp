package it.kimia.model;

/** Represents a raw row from analisi_prezzi / analisi_prezzi_voci_dettaglio. */
public class AnalisiRow {
    private String sheetName;
    private String voceCode;
    private int componentOrder;
    private String componentType;
    private String description;
    private String unitMeasure;
    private double quantity;
    private double unitPrice;
    private double totalPrice;

    public AnalisiRow() {}

    public String getSheetName()      { return sheetName; }
    public String getVoceCode()       { return voceCode; }
    public int    getComponentOrder() { return componentOrder; }
    public String getComponentType()  { return componentType; }
    public String getDescription()    { return description; }
    public String getUnitMeasure()    { return unitMeasure; }
    public double getQuantity()       { return quantity; }
    public double getUnitPrice()      { return unitPrice; }
    public double getTotalPrice()     { return totalPrice; }

    public void setSheetName(String v)      { this.sheetName = v; }
    public void setVoceCode(String v)       { this.voceCode = v; }
    public void setComponentOrder(int v)    { this.componentOrder = v; }
    public void setComponentType(String v)  { this.componentType = v; }
    public void setDescription(String v)    { this.description = v; }
    public void setUnitMeasure(String v)    { this.unitMeasure = v; }
    public void setQuantity(double v)       { this.quantity = v; }
    public void setUnitPrice(double v)      { this.unitPrice = v; }
    public void setTotalPrice(double v)     { this.totalPrice = v; }
}
