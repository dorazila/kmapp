package it.kimia.repository;

import it.kimia.db.Database;
import it.kimia.model.SavedOfferta;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class OffertaRepository {
    public List<SavedOfferta> findAll(String ownerUser) throws SQLException {
        List<SavedOfferta> list = new ArrayList<>();
        String sql = "SELECT id,owner_user,numero,data_offerta,cliente,cantiere,agente,email,tel,regione,scadenza_gg,note,trasporto,items_json,totale,created_at FROM offerte WHERE owner_user=? ORDER BY id DESC";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1, ownerUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }
    public Optional<SavedOfferta> findById(int id, String ownerUser) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT id,owner_user,numero,data_offerta,cliente,cantiere,agente,email,tel,regione,scadenza_gg,note,trasporto,items_json,totale,created_at FROM offerte WHERE id=? AND owner_user=?")) {
            ps.setInt(1,id); ps.setString(2, ownerUser); try(ResultSet rs=ps.executeQuery()){return rs.next()?Optional.of(map(rs)):Optional.empty();}
        }
    }
    public void save(SavedOfferta o, String ownerUser) throws SQLException {
        String sql = "INSERT INTO offerte (owner_user,numero,data_offerta,cliente,cantiere,agente,email,tel,regione,scadenza_gg,note,trasporto,items_json,totale) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1,ownerUser); ps.setString(2,o.getNumero()); ps.setString(3,o.getDataOfferta()); ps.setString(4,o.getCliente()); ps.setString(5,o.getCantiere()); ps.setString(6,o.getAgente()); ps.setString(7,o.getEmail()); ps.setString(8,o.getTel()); ps.setString(9,o.getRegione()); ps.setInt(10,o.getScadenzaGg()); ps.setString(11,o.getNote()); ps.setString(12,o.getTrasporto()); ps.setString(13,o.getItemsJson()); if(o.getTotale()==null) ps.setNull(14,Types.REAL); else ps.setDouble(14,o.getTotale()); ps.executeUpdate();
        }
    }
    public void delete(int id, String ownerUser) throws SQLException { try(PreparedStatement ps=Database.get().prepareStatement("DELETE FROM offerte WHERE id=? AND owner_user=?")){ps.setInt(1,id);ps.setString(2,ownerUser);ps.executeUpdate();} }
    private SavedOfferta map(ResultSet rs) throws SQLException { SavedOfferta o=new SavedOfferta(); o.setId(rs.getInt("id")); o.setOwnerUser(rs.getString("owner_user")); o.setNumero(rs.getString("numero")); o.setDataOfferta(rs.getString("data_offerta")); o.setCliente(rs.getString("cliente")); o.setCantiere(rs.getString("cantiere")); o.setAgente(rs.getString("agente")); o.setEmail(rs.getString("email")); o.setTel(rs.getString("tel")); o.setRegione(rs.getString("regione")); o.setScadenzaGg(rs.getInt("scadenza_gg")); o.setNote(rs.getString("note")); o.setTrasporto(rs.getString("trasporto")); o.setItemsJson(rs.getString("items_json")); double t=rs.getDouble("totale"); o.setTotale(rs.wasNull()?null:t); o.setCreatedAt(rs.getString("created_at")); return o; }
}
