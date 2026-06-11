package it.kimia.db;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class Database {
    private static final String DB_FILE_NAME = "kimia_data_normalizzato.db";
    private static final String AP_CSV_RESOURCE = "/it/kimia/AP_unico_con_lavorazioni.csv";
    private static Connection connection;
    private static String resolvedDbDirectory;

    private Database() {}

    public static synchronized void init() throws Exception {
        if (connection != null && !connection.isClosed()) return;
        String configured = System.getProperty("kimia.db.path", System.getenv().getOrDefault("KIMIA_DB_PATH", "./data/" + DB_FILE_NAME));
        Path dbPath = Path.of(configured).toAbsolutePath().normalize();
        if (!Files.exists(dbPath)) {
            Files.createDirectories(dbPath.getParent());
            try (InputStream in = Database.class.getResourceAsStream("/data/" + DB_FILE_NAME)) {
                if (in == null) throw new IllegalStateException("Database non trovato: " + dbPath);
                Files.copy(in, dbPath);
            }
        }
        resolvedDbDirectory = dbPath.getParent().toString();
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        connection.setAutoCommit(true);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA journal_mode = WAL");
        }
        createTablesIfNeeded();
        syncAnalisiPrezziFromCsv();
    }

    public static String getDbDirectory() { return resolvedDbDirectory; }
    public static String getDbPath() { return resolvedDbDirectory + File.separator + DB_FILE_NAME; }

    public static synchronized Connection get() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try { init(); } catch (Exception e) { throw new SQLException(e); }
        }
        return connection;
    }

    private static void createTablesIfNeeded() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS offerte (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero TEXT,
                    data_offerta TEXT NOT NULL,
                    cliente TEXT,
                    cantiere TEXT,
                    agente TEXT,
                    email TEXT,
                    tel TEXT,
                    regione TEXT,
                    scadenza_gg INTEGER,
                    note TEXT,
                    trasporto TEXT,
                    items_json TEXT NOT NULL,
                    totale REAL,
                    created_at TEXT DEFAULT (datetime('now','localtime'))
                )
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS analisi_prezzi_salvate (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    titolo TEXT,
                    sheet_name TEXT,
                    voce_code TEXT,
                    snapshot_json TEXT NOT NULL,
                    totale REAL,
                    created_at TEXT DEFAULT (datetime('now','localtime')),
                    numero_offerta TEXT,
                    cliente TEXT,
                    cantiere TEXT,
                    data_offerta TEXT,
                    agente_nome TEXT
                )
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS analisi_prezzi_voci_dettaglio (
                    sheet_name TEXT,
                    voce_code TEXT,
                    component_order INTEGER,
                    component_type TEXT,
                    description TEXT,
                    unit_measure TEXT,
                    quantity REAL,
                    unit_price REAL,
                    total_price REAL
                )
            """);
        }
        for (String col : new String[]{"numero_offerta","cliente","cantiere","data_offerta","agente_nome"}) {
            try (Statement st = connection.createStatement()) {
                st.execute("ALTER TABLE analisi_prezzi_salvate ADD COLUMN " + col + " TEXT");
            } catch (SQLException ignored) {}
        }
    }

    private static void syncAnalisiPrezziFromCsv() throws Exception {
        try (InputStream in = Database.class.getResourceAsStream(AP_CSV_RESOURCE)) {
            if (in == null) throw new IllegalStateException("CSV analisi prezzi non trovato: " + AP_CSV_RESOURCE);

            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                 Statement delete = connection.createStatement();
                 PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO analisi_prezzi_voci_dettaglio (sheet_name, voce_code, component_order, component_type, description, unit_measure, quantity, unit_price, total_price) VALUES (?,?,?,?,?,?,?,?,?)")) {

                String line = reader.readLine(); // header
                delete.executeUpdate("DELETE FROM analisi_prezzi_voci_dettaglio");

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() < 9) continue;

                    insert.setString(1, normalize(fields.get(0)));
                    insert.setString(2, normalize(fields.get(1)));
                    setNullableInt(insert, 3, fields.get(2));
                    insert.setString(4, normalize(fields.get(3)));
                    insert.setString(5, normalize(fields.get(4)));
                    insert.setString(6, normalize(fields.get(5)));
                    setNullableDouble(insert, 7, fields.get(6));
                    setNullableDouble(insert, 8, fields.get(7));
                    setNullableDouble(insert, 9, fields.get(8));
                    insert.addBatch();
                }

                insert.executeBatch();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (ch == ',' && !inQuotes) {
                out.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        out.add(current.toString());
        return out;
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void setNullableInt(PreparedStatement ps, int index, String raw) throws SQLException {
        String value = normalize(raw);
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
            return;
        }
        ps.setInt(index, Integer.parseInt(value));
    }

    private static void setNullableDouble(PreparedStatement ps, int index, String raw) throws SQLException {
        String value = normalize(raw);
        if (value == null) {
            ps.setNull(index, Types.REAL);
            return;
        }
        ps.setDouble(index, Double.parseDouble(value));
    }
}
