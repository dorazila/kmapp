package it.kimia.repository;

import it.kimia.db.Database;
import it.kimia.model.SavedOfferta;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class OffertaRepository {
    public List<SavedOfferta> findAll() throws SQLException {
        List<SavedOfferta> list = new ArrayList<>();
        String sql = "SELECT id,numero,data_offerta,cliente,cantiere,agente,email,tel,regione,scadenza_gg,note,trasporto,items_json,totale,created_at FROM offerte ORDER BY id DESC";
        try (PreparedStatement ps = Database.get().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }
    public Optional<SavedOfferta> findById(int id) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT id,numero,data_offerta,cliente,cantiere,agente,email,tel,regione,scadenza_gg,note,trasporto,items_json,totale,created_at FROM offerte WHERE id=?")) {
            ps.setInt(1,id); try(ResultSet rs=ps.executeQuery()){return rs.next()?Optional.of(map(rs)):Optional.empty();}
        }
    }
    public void save(SavedOfferta o) throws SQLException {
        String sql = "INSERT INTO offerte (numero,data_offerta,cliente,cantiere,agente,email,tel,regione,scadenza_gg,note,trasporto,items_json,totale) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1,o.getNumero()); ps.setString(2,o.getDataOfferta()); ps.setString(3,o.getCliente()); ps.setString(4,o.getCantiere()); ps.setString(5,o.getAgente()); ps.setString(6,o.getEmail()); ps.setString(7,o.getTel()); ps.setString(8,o.getRegione()); ps.setInt(9,o.getScadenzaGg()); ps.setString(10,o.getNote()); ps.setString(11,o.getTrasporto()); ps.setString(12,o.getItemsJson()); if(o.getTotale()==null) ps.setNull(13,Types.REAL); else ps.setDouble(13,o.getTotale()); ps.executeUpdate();
        }
    }
    public void delete(int id) throws SQLException { try(PreparedStatement ps=Database.get().prepareStatement("DELETE FROM offerte WHERE id=?")){ps.setInt(1,id);ps.executeUpdate();} }
    private SavedOfferta map(ResultSet rs) throws SQLException { SavedOfferta o=new SavedOfferta(); o.setId(rs.getInt("id")); o.setNumero(rs.getString("numero")); o.setDataOfferta(rs.getString("data_offerta")); o.setCliente(rs.getString("cliente")); o.setCantiere(rs.getString("cantiere")); o.setAgente(rs.getString("agente")); o.setEmail(rs.getString("email")); o.setTel(rs.getString("tel")); o.setRegione(rs.getString("regione")); o.setScadenzaGg(rs.getInt("scadenza_gg")); o.setNote(rs.getString("note")); o.setTrasporto(rs.getString("trasporto")); o.setItemsJson(rs.getString("items_json")); double t=rs.getDouble("totale"); o.setTotale(rs.wasNull()?null:t); o.setCreatedAt(rs.getString("created_at")); return o; }
}
