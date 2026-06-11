package it.kimia.model;

public class SavedOfferta {
    private int id;
    private String numero;
    private String dataOfferta;
    private String cliente;
    private String cantiere;
    private String agente;
    private String email;
    private String tel;
    private String regione;
    private int scadenzaGg;
    private String note;
    private String trasporto;
    private String itemsJson;
    private Double totale;
    private String createdAt;

    public int    getId()          { return id; }
    public String getNumero()      { return numero; }
    public String getDataOfferta() { return dataOfferta; }
    public String getCliente()     { return cliente; }
    public String getCantiere()    { return cantiere; }
    public String getAgente()      { return agente; }
    public String getEmail()       { return email; }
    public String getTel()         { return tel; }
    public String getRegione()     { return regione; }
    public int    getScadenzaGg()  { return scadenzaGg; }
    public String getNote()        { return note; }
    public String getTrasporto()   { return trasporto; }
    public String getItemsJson()   { return itemsJson; }
    public Double getTotale()      { return totale; }
    public String getCreatedAt()   { return createdAt; }

    public void setId(int v)           { this.id = v; }
    public void setNumero(String v)    { this.numero = v; }
    public void setDataOfferta(String v) { this.dataOfferta = v; }
    public void setCliente(String v)   { this.cliente = v; }
    public void setCantiere(String v)  { this.cantiere = v; }
    public void setAgente(String v)    { this.agente = v; }
    public void setEmail(String v)     { this.email = v; }
    public void setTel(String v)       { this.tel = v; }
    public void setRegione(String v)   { this.regione = v; }
    public void setScadenzaGg(int v)   { this.scadenzaGg = v; }
    public void setNote(String v)      { this.note = v; }
    public void setTrasporto(String v) { this.trasporto = v; }
    public void setItemsJson(String v) { this.itemsJson = v; }
    public void setTotale(Double v)    { this.totale = v; }
    public void setCreatedAt(String v) { this.createdAt = v; }
}
