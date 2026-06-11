package it.kimia.model;

public class WorkSystemItem {
    private String workCategoryName;
    private String systemName;
    private String productCode;
    private double expectedQty;

    public WorkSystemItem() {}

    public WorkSystemItem(String workCategoryName, String systemName,
                          String productCode, double expectedQty) {
        this.workCategoryName = workCategoryName;
        this.systemName = systemName;
        this.productCode = productCode;
        this.expectedQty = expectedQty;
    }

    public String getWorkCategoryName() { return workCategoryName; }
    public String getSystemName()       { return systemName; }
    public String getProductCode()      { return productCode; }
    public double getExpectedQty()      { return expectedQty; }

    public void setWorkCategoryName(String v) { this.workCategoryName = v; }
    public void setSystemName(String v)       { this.systemName = v; }
    public void setProductCode(String v)      { this.productCode = v; }
    public void setExpectedQty(double v)      { this.expectedQty = v; }
}
