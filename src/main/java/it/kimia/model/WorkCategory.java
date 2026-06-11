package it.kimia.model;

public class WorkCategory {
    private String workCategoryName;
    private int sortOrder;

    public WorkCategory() {}

    public WorkCategory(String workCategoryName, int sortOrder) {
        this.workCategoryName = workCategoryName;
        this.sortOrder = sortOrder;
    }

    public String getWorkCategoryName() { return workCategoryName; }
    public int    getSortOrder()        { return sortOrder; }

    public void setWorkCategoryName(String v) { this.workCategoryName = v; }
    public void setSortOrder(int v)           { this.sortOrder = v; }
}
