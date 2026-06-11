package it.kimia.model;

public class WorkSystem {
    private String workCategoryName;
    private String systemName;
    private int sortOrder;

    public WorkSystem() {}

    public WorkSystem(String workCategoryName, String systemName, int sortOrder) {
        this.workCategoryName = workCategoryName;
        this.systemName = systemName;
        this.sortOrder = sortOrder;
    }

    public String getWorkCategoryName() { return workCategoryName; }
    public String getSystemName()       { return systemName; }
    public int    getSortOrder()        { return sortOrder; }

    public void setWorkCategoryName(String v) { this.workCategoryName = v; }
    public void setSystemName(String v)       { this.systemName = v; }
    public void setSortOrder(int v)           { this.sortOrder = v; }
}
