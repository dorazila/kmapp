package it.kimia.repository;

import it.kimia.db.Database;
import it.kimia.model.AnalisiComponent;
import it.kimia.model.SavedAnalisi;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class AnalisiRepository {
    public Map<String, List<String>> sections() throws SQLException {
        Map<String, List<String>> map = new LinkedHashMap<>();
        String sql = "SELECT DISTINCT sheet_name, voce_code FROM analisi_prezzi_voci_dettaglio ORDER BY sheet_name, voce_code";
        try (PreparedStatement ps = Database.get().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.computeIfAbsent(rs.getString(1), k -> new ArrayList<>()).add(rs.getString(2));
        }
        return map;
    }
    public List<AnalisiComponent> components(String voceCode) throws SQLException {
        List<AnalisiComponent> list = new ArrayList<>();
        String sql = "SELECT voce_code, component_order, sheet_name, component_type, description, unit_measure, quantity, unit_price, total_price FROM analisi_prezzi_voci_dettaglio WHERE voce_code=? ORDER BY COALESCE(source_order, component_order), component_order";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1, voceCode);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapComponent(rs)); }
        }
        return list;
    }
    public List<SavedAnalisi> saved() throws SQLException {
        List<SavedAnalisi> list = new ArrayList<>();
        String sql = "SELECT id,titolo,sheet_name,voce_code,snapshot_json,totale,created_at,numero_offerta,cliente,cantiere,data_offerta,agente_nome FROM analisi_prezzi_salvate ORDER BY id DESC";
        try (PreparedStatement ps = Database.get().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { SavedAnalisi a = new SavedAnalisi(); a.setId(rs.getInt("id")); a.setTitolo(rs.getString("titolo")); a.setSheetName(rs.getString("sheet_name")); a.setVoceCode(rs.getString("voce_code")); a.setSnapshotJson(rs.getString("snapshot_json")); double t=rs.getDouble("totale"); a.setTotale(rs.wasNull()?null:t); a.setCreatedAt(rs.getString("created_at")); a.setNumeroOfferta(rs.getString("numero_offerta")); a.setCliente(rs.getString("cliente")); a.setCantiere(rs.getString("cantiere")); a.setDataOfferta(rs.getString("data_offerta")); a.setAgenteNome(rs.getString("agente_nome")); list.add(a); }
        }
        return list;
    }
    public SavedAnalisi findSavedById(int id) throws SQLException {
        String sql = "SELECT id,titolo,sheet_name,voce_code,snapshot_json,totale,created_at FROM analisi_prezzi_salvate WHERE id=?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Analisi non trovata: " + id);
                SavedAnalisi a = new SavedAnalisi();
                a.setId(rs.getInt("id")); a.setTitolo(rs.getString("titolo"));
                a.setSheetName(rs.getString("sheet_name")); a.setVoceCode(rs.getString("voce_code"));
                a.setSnapshotJson(rs.getString("snapshot_json"));
                double t = rs.getDouble("totale"); a.setTotale(rs.wasNull() ? null : t);
                a.setCreatedAt(rs.getString("created_at"));
                return a;
            }
        }
    }

    public void saveAnalisi(String titolo, String voceCode, String sheetName, String snapshotJson, Double totale) throws SQLException {
        String sql = "INSERT INTO analisi_prezzi_salvate (titolo, voce_code, sheet_name, snapshot_json, totale) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1, titolo);
            ps.setString(2, voceCode);
            ps.setString(3, sheetName);
            ps.setString(4, snapshotJson);
            if (totale != null) ps.setDouble(5, totale); else ps.setNull(5, Types.REAL);
            ps.executeUpdate();
        }
    }

    private AnalisiComponent mapComponent(ResultSet rs) throws SQLException {
        AnalisiComponent c = new AnalisiComponent(); c.setVoce_code(rs.getString("voce_code")); c.setComponent_order(rs.getInt("component_order")); c.setSheet_name(rs.getString("sheet_name")); c.setComponent_type(rs.getString("component_type")); c.setDescription(rs.getString("description")); c.setUnit_measure(rs.getString("unit_measure")); c.setQuantity(rs.getDouble("quantity")); c.setUnit_price(rs.getDouble("unit_price")); c.setTotal_price(rs.getDouble("total_price")); return c;
    }

}
