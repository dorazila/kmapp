package it.kimia.model;

public class Product {
    private String productCode;
    private String productName;
    private String description;
    private String unitMeasure;
    private String categoryName;
    private double listinoPrice;
    private double discountRate;
    private double netPrice;
    private String listinoType; // "STA", "FST", "NLIS"

    public Product() {}

    public Product(String productCode, String productName, String description,
                   String unitMeasure, String categoryName,
                   double listinoPrice, double discountRate, double netPrice) {
        this.productCode = productCode;
        this.productName = productName;
        this.description = description;
        this.unitMeasure = unitMeasure;
        this.categoryName = categoryName;
        this.listinoPrice = listinoPrice;
        this.discountRate = discountRate;
        this.netPrice = netPrice;
    }

    public String getProductCode()   { return productCode; }
    public String getProductName()   { return productName; }
    public String getDescription()   { return description; }
    public String getUnitMeasure()   { return unitMeasure; }
    public String getCategoryName()  { return categoryName; }
    public double getListinoPrice()  { return listinoPrice; }
    public double getDiscountRate()  { return discountRate; }
    public double getNetPrice()      { return netPrice; }
    public String getListinoType()   { return listinoType; }

    public void setProductCode(String v)  { this.productCode = v; }
    public void setProductName(String v)  { this.productName = v; }
    public void setDescription(String v)  { this.description = v; }
    public void setUnitMeasure(String v)  { this.unitMeasure = v; }
    public void setCategoryName(String v) { this.categoryName = v; }
    public void setListinoPrice(double v) { this.listinoPrice = v; }
    public void setDiscountRate(double v) { this.discountRate = v; }
    public void setNetPrice(double v)     { this.netPrice = v; }
    public void setListinoType(String v)  { this.listinoType = v; }
}
