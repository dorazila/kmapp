package it.kimia.db;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Database {
    private static final String DB_FILE_NAME = "kimia_data_normalizzato.db";
    private static final String AP_CSV_RESOURCE = "/it/kimia/AP_unico_con_lavorazioni.csv";
    private static final String AP_DESCRIZIONI_RESOURCE = "/it/kimia/AP_descrizioni.csv";
    private static final String LISTINO_CODICI_RESOURCE = "/it/kimia/listino_codici.csv";
    private static final String AP_CSV_FILE = "AP_unico_con_lavorazioni.csv";
    private static final String AP_DESCRIZIONI_FILE = "AP_descrizioni.csv";
    private static final String LISTINO_CODICI_FILE = "listino_codici.csv";
    private static final String PRODUCTS_CSV_FILE = "products.csv";
    private static final String PRODUCT_CATEGORIES_CSV_FILE = "product_categories.csv";
    private static final String ANALISI_PREZZI_CSV_FILE = "analisi_prezzi.csv";
    private static final String LISTINO_CSV_FILE = "listino.csv";
    private static Connection connection;
    private static String resolvedDbDirectory;
    private static Path csvDirectory;

    private Database() {}

    public static synchronized void init() throws Exception {
        if (connection != null && !connection.isClosed()) return;
        String configured = System.getProperty("kimia.db.path", System.getenv().getOrDefault("KIMIA_DB_PATH", "./data/" + DB_FILE_NAME));
        Path dbPath = Path.of(configured).toAbsolutePath().normalize();
        if (!Files.exists(dbPath)) {
            Files.createDirectories(dbPath.getParent());
            // Seed from classpath if available; otherwise SQLite creates an empty DB
            try (InputStream in = Database.class.getResourceAsStream("/data/" + DB_FILE_NAME)) {
                if (in != null) Files.copy(in, dbPath);
            }
        }
        resolvedDbDirectory = dbPath.getParent().toString();
        csvDirectory = resolveCsvDirectory(dbPath.getParent());
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        connection.setAutoCommit(true);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA journal_mode = WAL");
        }
        createTablesIfNeeded();
        ensureEditableCsvFiles();
        syncProductsFromCsv();
        syncAnalisiPrezziFromCsv();
        syncAnalisiDescrizioniFromCsv();
    }

    public static String getDbDirectory() { return resolvedDbDirectory; }
    public static String getDbPath() { return resolvedDbDirectory + File.separator + DB_FILE_NAME; }
    public static String getCsvDirectory() { return csvDirectory != null ? csvDirectory.toString() : null; }

    public static synchronized Connection get() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try { init(); } catch (Exception e) { throw new SQLException(e); }
        }
        return connection;
    }

    private static void createTablesIfNeeded() throws SQLException {
        String defaultOwner = sqlLiteral("admin");
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS app_users (
                    username TEXT PRIMARY KEY,
                    password_hash TEXT NOT NULL,
                    display_name TEXT,
                    created_at TEXT DEFAULT (datetime('now','localtime'))
                )
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS offerte (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_user TEXT NOT NULL DEFAULT 'admin',
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
                    owner_user TEXT NOT NULL DEFAULT 'admin',
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
                    source_order INTEGER,
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
            st.execute("""
                CREATE TABLE IF NOT EXISTS analisi_prezzi_voci_meta (
                    sheet_name TEXT NOT NULL,
                    voce_code TEXT NOT NULL,
                    intro_description TEXT,
                    PRIMARY KEY (sheet_name, voce_code)
                )
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS product_categories (
                    category_name TEXT PRIMARY KEY,
                    sort_order INTEGER NOT NULL
                )
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    product_code TEXT PRIMARY KEY,
                    source_code TEXT,
                    product_name TEXT NOT NULL,
                    description TEXT,
                    unit_measure TEXT,
                    category_name TEXT NOT NULL,
                    listino_price NUMERIC(12,6) NOT NULL,
                    discount_rate NUMERIC(8,6) NOT NULL,
                    net_price NUMERIC(12,6) NOT NULL,
                    listino_type TEXT NOT NULL DEFAULT 'NLIS',
                    has_listino_override INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (category_name) REFERENCES product_categories(category_name)
                )
            """);
        }
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE analisi_prezzi_voci_dettaglio ADD COLUMN source_order INTEGER");
        } catch (SQLException ignored) {}
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE products ADD COLUMN listino_type TEXT NOT NULL DEFAULT 'NLIS'");
        } catch (SQLException ignored) {}
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE offerte ADD COLUMN owner_user TEXT NOT NULL DEFAULT " + defaultOwner);
        } catch (SQLException ignored) {}
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE analisi_prezzi_salvate ADD COLUMN owner_user TEXT NOT NULL DEFAULT " + defaultOwner);
        } catch (SQLException ignored) {}
        for (String col : new String[]{"numero_offerta","cliente","cantiere","data_offerta","agente_nome"}) {
            try (Statement st = connection.createStatement()) {
                st.execute("ALTER TABLE analisi_prezzi_salvate ADD COLUMN " + col + " TEXT");
            } catch (SQLException ignored) {}
        }
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE INDEX IF NOT EXISTS idx_offerte_owner_created ON offerte(owner_user, created_at DESC)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_analisi_salvate_owner_created ON analisi_prezzi_salvate(owner_user, created_at DESC)");
        }
        ensureInitialUsers();
    }

    private static void ensureInitialUsers() throws SQLException {
        insertUserIfMissing("admin", "admin", "admin");

        String configuredUser = initialUsername();
        if (!"admin".equalsIgnoreCase(configuredUser)) {
            insertUserIfMissing(configuredUser, initialPassword(), initialDisplayName(configuredUser));
        }
    }

    private static void insertUserIfMissing(String username, String password, String displayName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO app_users (username, password_hash, display_name)
                VALUES (?,?,?)
                ON CONFLICT(username) DO NOTHING
             """)) {
            ps.setString(1, username);
            ps.setString(2, it.kimia.util.Passwords.hash(password));
            ps.setString(3, displayName);
            ps.executeUpdate();
        }
    }

    private static String initialUsername() {
        String value = normalize(System.getProperty(
            "kimia.admin.username",
            System.getenv().getOrDefault("KIMIA_ADMIN_USER", "admin")
        ));
        return value != null ? value : "admin";
    }

    private static String initialPassword() {
        String value = System.getProperty(
            "kimia.admin.password",
            System.getenv().getOrDefault("KIMIA_ADMIN_PASSWORD", "admin")
        );
        return value == null || value.isBlank() ? "admin" : value;
    }

    private static String initialDisplayName(String username) {
        String value = System.getProperty(
            "kimia.admin.display-name",
            System.getenv().getOrDefault("KIMIA_ADMIN_DISPLAY_NAME", username)
        );
        return value == null || value.isBlank() ? username : value.trim();
    }

    private static String sqlLiteral(String value) {
        String normalized = value == null || value.isBlank() ? "admin" : value;
        return "'" + normalized.replace("'", "''") + "'";
    }

    private static Path resolveCsvDirectory(Path dbDirectory) {
        String configured = System.getProperty(
            "kimia.csv.dir",
            System.getenv().getOrDefault("KIMIA_CSV_DIR", dbDirectory.resolve("csv").toString())
        );
        return Path.of(configured).toAbsolutePath().normalize();
    }

    private static Path csvFile(String fileName) {
        return csvDirectory.resolve(fileName);
    }

    private static void ensureEditableCsvFiles() throws Exception {
        Files.createDirectories(csvDirectory);
        ensureAnalisiPrezziCsvIfMissing(csvFile(ANALISI_PREZZI_CSV_FILE));
        ensureListinoCsvIfMissing(csvFile(LISTINO_CSV_FILE));
    }

    private static void copyResourceCsvIfMissing(String resourcePath, Path target) throws Exception {
        if (Files.exists(target)) return;
        try (InputStream in = Database.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new IllegalStateException("CSV risorsa non trovato: " + resourcePath);
            Files.copy(in, target);
        }
    }

    private static BufferedReader openCsvReader(Path path, String resourcePath) throws Exception {
        if (path != null && Files.exists(path)) return Files.newBufferedReader(path, StandardCharsets.UTF_8);
        InputStream in = Database.class.getResourceAsStream(resourcePath);
        if (in == null) throw new IllegalStateException("CSV risorsa non trovato: " + resourcePath);
        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    private static void ensureAnalisiPrezziCsvIfMissing(Path target) throws Exception {
        if (Files.exists(target)) return;
        Map<String, String> introDescriptions = readLegacyAnalisiDescriptions();
        try (BufferedReader reader = openCsvReader(csvFile(AP_CSV_FILE), AP_CSV_RESOURCE);
             BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            writeCsvRow(writer, "sheet_name", "voce_code", "descr_code", "intro_description", "component_order",
                "component_type", "description", "unit_measure", "quantity", "unit_price", "total_price");
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 9) continue;
                String key = csvMetaKey(fields.get(0), fields.get(1));
                String[] voceParts = splitVoceCode(fields.get(1));
                writeCsvRow(writer,
                    fields.get(0), voceParts[0], voceParts[1], introDescriptions.getOrDefault(key, ""),
                    fields.get(2), fields.get(3), fields.get(4), fields.get(5),
                    fields.get(6), fields.get(7), fields.get(8));
            }
        }
    }

    private static Map<String, String> readLegacyAnalisiDescriptions() throws Exception {
        Map<String, String> out = new LinkedHashMap<>();
        try (BufferedReader reader = openCsvReader(csvFile(AP_DESCRIZIONI_FILE), AP_DESCRIZIONI_RESOURCE)) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 3) continue;
                out.put(csvMetaKey(fields.get(0), fields.get(1)), fields.get(2));
            }
        }
        return out;
    }

    private static void ensureListinoCsvIfMissing(Path target) throws Exception {
        if (Files.exists(target)) return;
        Map<String, Integer> categoryOrders = readLegacyCategoryOrders();
        Map<String, String> listinoTypes = readLegacyListinoTypes();
        int[] nextCategoryOrder = {categoryOrders.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1};

        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            writeCsvRow(writer, "product_code", "source_code", "product_name", "description", "unit_measure",
                "category_name", "category_sort_order", "listino_price", "discount_rate", "net_price",
                "listino_type", "has_listino_override");
            Path legacyProducts = csvFile(PRODUCTS_CSV_FILE);
            if (Files.exists(legacyProducts)) {
                try (BufferedReader reader = Files.newBufferedReader(legacyProducts, StandardCharsets.UTF_8)) {
                    String line = reader.readLine(); // header
                    while ((line = reader.readLine()) != null) {
                        if (line.isBlank()) continue;
                        List<String> fields = parseCsvLine(line);
                        if (fields.size() < 10) continue;
                        String productCode = normalize(fields.get(0));
                        String categoryName = normalize(fields.get(5));
                        if (productCode == null || categoryName == null) continue;
                        int categoryOrder = categoryOrders.computeIfAbsent(categoryName, ignored -> nextCategoryOrder[0]++);
                        writeCsvRow(writer,
                            fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5),
                            categoryOrder, fields.get(6), fields.get(7), fields.get(8),
                            listinoTypes.getOrDefault(productCode, "NLIS"), fields.get(9));
                    }
                }
                return;
            }

            try (PreparedStatement ps = connection.prepareStatement("""
                     SELECT product_code, source_code, product_name, description, unit_measure, category_name,
                            listino_price, discount_rate, net_price, listino_type, has_listino_override
                     FROM products
                     ORDER BY category_name, product_name, product_code
                 """);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String productCode = rs.getString("product_code");
                    String categoryName = rs.getString("category_name");
                    int categoryOrder = categoryOrders.computeIfAbsent(categoryName, ignored -> nextCategoryOrder[0]++);
                    writeCsvRow(writer,
                        productCode,
                        rs.getString("source_code"),
                        rs.getString("product_name"),
                        rs.getString("description"),
                        rs.getString("unit_measure"),
                        categoryName,
                        categoryOrder,
                        rs.getDouble("listino_price"),
                        rs.getDouble("discount_rate"),
                        rs.getDouble("net_price"),
                        listinoTypes.getOrDefault(productCode, normalizeListinoType(rs.getString("listino_type"))),
                        rs.getInt("has_listino_override"));
                }
            }
        }
    }

    private static Map<String, Integer> readLegacyCategoryOrders() throws Exception {
        Map<String, Integer> out = new LinkedHashMap<>();
        Path legacyCategories = csvFile(PRODUCT_CATEGORIES_CSV_FILE);
        if (Files.exists(legacyCategories)) {
            try (BufferedReader reader = Files.newBufferedReader(legacyCategories, StandardCharsets.UTF_8)) {
                String line = reader.readLine(); // header
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() < 2) continue;
                    String categoryName = normalize(fields.get(0));
                    if (categoryName != null) out.put(categoryName, parseIntOrZero(fields.get(1)));
                }
            }
            return out;
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT category_name, sort_order FROM product_categories ORDER BY sort_order, category_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.put(rs.getString("category_name"), rs.getInt("sort_order"));
        }
        return out;
    }

    private static Map<String, String> readLegacyListinoTypes() throws Exception {
        Map<String, String> out = new LinkedHashMap<>();
        try (BufferedReader reader = openCsvReader(csvFile(LISTINO_CODICI_FILE), LISTINO_CODICI_RESOURCE)) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 2) continue;
                String productCode = normalize(fields.get(0));
                if (productCode != null) out.put(productCode, normalizeListinoType(fields.get(1)));
            }
        }
        return out;
    }

    private static void exportProductCategoriesCsvIfMissing(Path target) throws Exception {
        if (Files.exists(target)) return;
        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8);
             PreparedStatement ps = connection.prepareStatement("SELECT category_name, sort_order FROM product_categories ORDER BY sort_order, category_name");
             ResultSet rs = ps.executeQuery()) {
            writeCsvRow(writer, "category_name", "sort_order");
            while (rs.next()) writeCsvRow(writer, rs.getString("category_name"), rs.getInt("sort_order"));
        }
    }

    private static void exportProductsCsvIfMissing(Path target) throws Exception {
        if (Files.exists(target)) return;
        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8);
             PreparedStatement ps = connection.prepareStatement("""
                 SELECT product_code, source_code, product_name, description, unit_measure, category_name,
                        listino_price, discount_rate, net_price, has_listino_override
                 FROM products
                 ORDER BY category_name, product_name, product_code
             """);
             ResultSet rs = ps.executeQuery()) {
            writeCsvRow(writer, "product_code", "source_code", "product_name", "description", "unit_measure",
                "category_name", "listino_price", "discount_rate", "net_price", "has_listino_override");
            while (rs.next()) {
                writeCsvRow(writer,
                    rs.getString("product_code"),
                    rs.getString("source_code"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("unit_measure"),
                    rs.getString("category_name"),
                    rs.getDouble("listino_price"),
                    rs.getDouble("discount_rate"),
                    rs.getDouble("net_price"),
                    rs.getInt("has_listino_override"));
            }
        }
    }

    private static void syncProductsFromCsv() throws Exception {
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement upsertCategory = connection.prepareStatement("""
                 INSERT INTO product_categories (category_name, sort_order) VALUES (?,?)
                 ON CONFLICT(category_name) DO UPDATE SET sort_order=excluded.sort_order
             """);
             PreparedStatement upsertProduct = connection.prepareStatement("""
                 INSERT INTO products (
                     product_code, source_code, product_name, description, unit_measure, category_name,
                     listino_price, discount_rate, net_price, listino_type, has_listino_override
                 ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                 ON CONFLICT(product_code) DO UPDATE SET
                     source_code=excluded.source_code,
                     product_name=excluded.product_name,
                     description=excluded.description,
                     unit_measure=excluded.unit_measure,
                     category_name=excluded.category_name,
                     listino_price=excluded.listino_price,
                     discount_rate=excluded.discount_rate,
                     net_price=excluded.net_price,
                     listino_type=excluded.listino_type,
                     has_listino_override=excluded.has_listino_override
             """)) {

            Map<String, Integer> categoryOrders = new LinkedHashMap<>();
            int nextCategoryOrder = categoryOrders.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;

            try (BufferedReader reader = Files.newBufferedReader(csvFile(LISTINO_CSV_FILE), StandardCharsets.UTF_8)) {
                String line = reader.readLine(); // header
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() < 12) continue;

                    String productCode = normalize(fields.get(0));
                    String productName = normalize(fields.get(2));
                    String categoryName = normalize(fields.get(5));
                    if (productCode == null || productName == null || categoryName == null) continue;

                    int categoryOrder = parseIntOrZero(fields.get(6));
                    if (categoryOrder <= 0) categoryOrder = categoryOrders.getOrDefault(categoryName, nextCategoryOrder);
                    if (!categoryOrders.containsKey(categoryName) || categoryOrders.get(categoryName) != categoryOrder) {
                        categoryOrders.put(categoryName, categoryOrder);
                        upsertCategory.setString(1, categoryName);
                        upsertCategory.setInt(2, categoryOrder);
                        upsertCategory.executeUpdate();
                        if (categoryOrder >= nextCategoryOrder) nextCategoryOrder = categoryOrder + 1;
                    }

                    double listinoPrice = parseDoubleOrZero(fields.get(7));
                    double discountRate = parseDoubleOrZero(fields.get(8));
                    double netPrice = normalize(fields.get(9)) != null
                        ? parseDoubleOrZero(fields.get(9))
                        : listinoPrice * (1.0 - discountRate);

                    upsertProduct.setString(1, productCode);
                    upsertProduct.setString(2, normalize(fields.get(1)));
                    upsertProduct.setString(3, productName);
                    upsertProduct.setString(4, normalize(fields.get(3)));
                    upsertProduct.setString(5, normalize(fields.get(4)));
                    upsertProduct.setString(6, categoryName);
                    upsertProduct.setDouble(7, listinoPrice);
                    upsertProduct.setDouble(8, discountRate);
                    upsertProduct.setDouble(9, netPrice);
                    upsertProduct.setString(10, normalizeListinoType(fields.get(10)));
                    upsertProduct.setInt(11, parseIntOrZero(fields.get(11)));
                    upsertProduct.addBatch();
                }
            }

            upsertProduct.executeBatch();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private static Map<String, String> readListinoTypesCsv() throws Exception {
        Map<String, String> out = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(csvFile(LISTINO_CODICI_FILE), StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 2) continue;
                String productCode = normalize(fields.get(0));
                String listinoType = normalizeListinoType(fields.get(1));
                if (productCode != null) out.put(productCode, listinoType);
            }
        }
        return out;
    }

    private static Map<String, Integer> readCategoryCsv() throws Exception {
        Map<String, Integer> out = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(csvFile(PRODUCT_CATEGORIES_CSV_FILE), StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 2) continue;
                String categoryName = normalize(fields.get(0));
                if (categoryName == null) continue;
                out.put(categoryName, parseIntOrZero(fields.get(1)));
            }
        }
        return out;
    }

    private static void syncAnalisiPrezziFromCsv() throws Exception {
        Path csvPath = csvFile(ANALISI_PREZZI_CSV_FILE);
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
                 Statement delete = connection.createStatement();
                 PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO analisi_prezzi_voci_dettaglio (source_order, sheet_name, voce_code, component_order, component_type, description, unit_measure, quantity, unit_price, total_price) VALUES (?,?,?,?,?,?,?,?,?,?)")) {

                String line = reader.readLine(); // header
                List<String> header = line != null ? parseCsvLine(line) : List.of();
                boolean hasDescrCode = header.contains("descr_code");
                int offset = hasDescrCode ? 1 : 0;
                delete.executeUpdate("DELETE FROM analisi_prezzi_voci_dettaglio");

                int sourceOrder = 1;
                String previousLavorazioneKey = null;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() < 10 + offset) continue;

                    int currentSourceOrder = sourceOrder++;
                    String sheetName = normalize(fields.get(0));
                    String voceCode = hasDescrCode ? combineVoceCode(fields.get(1), fields.get(2)) : normalize(fields.get(1));
                    String componentType = normalize(fields.get(4 + offset));
                    String description = normalize(fields.get(5 + offset));
                    String lavorazioneKey = duplicateLavorazioneKey(sheetName, voceCode, componentType, description);
                    if (lavorazioneKey != null && lavorazioneKey.equals(previousLavorazioneKey)) continue;
                    previousLavorazioneKey = lavorazioneKey;

                    insert.setInt(1, currentSourceOrder);
                    insert.setString(2, sheetName);
                    insert.setString(3, voceCode);
                    setNullableInt(insert, 4, fields.get(3 + offset));
                    insert.setString(5, componentType);
                    insert.setString(6, description);
                    insert.setString(7, normalize(fields.get(6 + offset)));
                    setNullableDouble(insert, 8, fields.get(7 + offset));
                    setNullableDouble(insert, 9, fields.get(8 + offset));
                    setNullableDouble(insert, 10, fields.get(9 + offset));
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

    private static void syncAnalisiDescrizioniFromCsv() throws Exception {
        Path csvPath = csvFile(ANALISI_PREZZI_CSV_FILE);
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
                 Statement delete = connection.createStatement();
                 PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO analisi_prezzi_voci_meta (sheet_name, voce_code, intro_description) VALUES (?,?,?)")) {

                String line = reader.readLine(); // header
                List<String> header = line != null ? parseCsvLine(line) : List.of();
                boolean hasDescrCode = header.contains("descr_code");
                int introIndex = hasDescrCode ? 3 : 2;
                delete.executeUpdate("DELETE FROM analisi_prezzi_voci_meta");
                Map<String, String[]> descriptions = new LinkedHashMap<>();

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() <= introIndex) continue;

                    String sheetName = normalize(fields.get(0));
                    String voceCode = hasDescrCode ? combineVoceCode(fields.get(1), fields.get(2)) : normalize(fields.get(1));
                    if (sheetName == null || voceCode == null) continue;
                    descriptions.putIfAbsent(csvMetaKey(sheetName, voceCode), new String[]{sheetName, voceCode, normalize(fields.get(introIndex))});
                }

                for (String[] description : descriptions.values()) {
                    insert.setString(1, description[0]);
                    insert.setString(2, description[1]);
                    insert.setString(3, description[2]);
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

    private static String csvMetaKey(String sheetName, String voceCode) {
        return (sheetName != null ? sheetName.trim() : "") + "\u001F" + (voceCode != null ? voceCode.trim() : "");
    }

    private static String[] splitVoceCode(String raw) {
        String value = normalize(raw);
        if (value == null) return new String[]{"", ""};
        int separator = value.indexOf(" - ");
        if (separator < 0) separator = value.indexOf('-');
        if (separator < 0) return new String[]{value, ""};
        return new String[]{
            value.substring(0, separator).trim(),
            value.substring(separator + (value.startsWith(" - ", separator) ? 3 : 1)).trim()
        };
    }

    private static String combineVoceCode(String code, String description) {
        String normalizedCode = normalize(code);
        String normalizedDescription = normalize(description);
        if (normalizedCode == null) return null;
        if (normalizedDescription == null) return normalizedCode;
        return normalizedCode + " - " + normalizedDescription;
    }

    private static String normalizeListinoType(String value) {
        String normalized = normalize(value);
        if (normalized == null) return "NLIS";
        normalized = normalized.toUpperCase();
        return ("STA".equals(normalized) || "FST".equals(normalized) || "NLIS".equals(normalized)) ? normalized : "NLIS";
    }

    private static int parseIntOrZero(String raw) {
        String value = normalize(raw);
        if (value == null) return 0;
        return Integer.parseInt(value);
    }

    private static double parseDoubleOrZero(String raw) {
        String value = normalize(raw);
        if (value == null) return 0.0;
        return Double.parseDouble(normalizeDecimal(value));
    }

    private static String normalizeDecimal(String value) {
        String normalized = value.trim();
        if (normalized.contains(",") && normalized.contains(".")) {
            normalized = normalized.replace(".", "").replace(",", ".");
        } else if (normalized.contains(",")) {
            normalized = normalized.replace(",", ".");
        }
        return normalized;
    }

    private static void writeCsvRow(BufferedWriter writer, Object... values) throws Exception {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) writer.write(",");
            writer.write(csvEscape(values[i]));
        }
        writer.newLine();
    }

    private static String csvEscape(Object raw) {
        if (raw == null) return "";
        String value = raw.toString().replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
        if (value.contains(",") || value.contains("\"")) return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    private static String duplicateLavorazioneKey(String sheetName, String voceCode, String componentType, String description) {
        if (!"lavorazione".equalsIgnoreCase(componentType) || description == null) return null;
        return String.join("\u001F",
            sheetName != null ? sheetName : "",
            voceCode != null ? voceCode : "",
            description);
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
        ps.setDouble(index, Double.parseDouble(normalizeDecimal(value)));
    }
}
