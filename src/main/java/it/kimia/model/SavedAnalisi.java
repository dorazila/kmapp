package it.kimia.model;

public class SavedAnalisi {
    private int id;
    private String titolo;
    private String sheetName;
    private String voceCode;
    private String snapshotJson;
    private Double totale;
    private String createdAt;
    private String numeroOfferta;
    private String cliente;
    private String cantiere;
    private String dataOfferta;
    private String agenteNome;

    public int    getId()             { return id; }
    public String getTitolo()         { return titolo; }
    public String getSheetName()      { return sheetName; }
    public String getVoceCode()       { return voceCode; }
    public String getSnapshotJson()   { return snapshotJson; }
    public Double getTotale()         { return totale; }
    public String getCreatedAt()      { return createdAt; }
    public String getNumeroOfferta()  { return numeroOfferta; }
    public String getCliente()        { return cliente; }
    public String getCantiere()       { return cantiere; }
    public String getDataOfferta()    { return dataOfferta; }
    public String getAgenteNome()     { return agenteNome; }

    public void setId(int v)              { this.id = v; }
    public void setTitolo(String v)       { this.titolo = v; }
    public void setSheetName(String v)    { this.sheetName = v; }
    public void setVoceCode(String v)     { this.voceCode = v; }
    public void setSnapshotJson(String v) { this.snapshotJson = v; }
    public void setTotale(Double v)       { this.totale = v; }
    public void setCreatedAt(String v)    { this.createdAt = v; }
    public void setNumeroOfferta(String v){ this.numeroOfferta = v; }
    public void setCliente(String v)      { this.cliente = v; }
    public void setCantiere(String v)     { this.cantiere = v; }
    public void setDataOfferta(String v)  { this.dataOfferta = v; }
    public void setAgenteNome(String v)   { this.agenteNome = v; }
}
