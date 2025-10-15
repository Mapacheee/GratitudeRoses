package me.mapacheee.gratituderoses.storage;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/* This class if to persist launches using HikariCP with SQLite/MySQL and query totals and first launch timestamps */

@Service
public class StorageService {
    private final Plugin plugin;
    private final String dbPath;
    private final me.mapacheee.gratituderoses.shared.ConfigService cfg;

    private HikariDataSource dataSource;
    private String dialect; // SQLITE or MYSQL

    @Inject
    public StorageService(Plugin plugin, me.mapacheee.gratituderoses.shared.ConfigService cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        File data = plugin.getDataFolder();
        if (!data.exists()) data.mkdirs();
        this.dbPath = new File(data, cfg.dbFile()).getAbsolutePath();
        initPool();
        initSchema();
    }

    private void initPool() {
        this.dialect = cfg.dbType();
        HikariConfig hc = new HikariConfig();
        if ("MYSQL".equalsIgnoreCase(dialect)) {
            String host = cfg.dbHost();
            int port = cfg.dbPort();
            String db = cfg.dbName();
            String params = cfg.dbParams();
            StringBuilder url = new StringBuilder("jdbc:mysql://").append(host).append(":").append(port).append("/").append(db);
            if (params != null && !params.isBlank()) {
                if (!params.startsWith("?")) url.append('?');
                url.append(params);
            } else {
                url.append("?useSSL=").append(cfg.dbUseSsl()).append("&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8");
            }
            hc.setJdbcUrl(url.toString());
            hc.setUsername(cfg.dbUser());
            hc.setPassword(cfg.dbPassword());
            hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hc.addDataSourceProperty("cachePrepStmts", "true");
            hc.addDataSourceProperty("prepStmtCacheSize", "250");
            hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            hc.setJdbcUrl("jdbc:sqlite:" + dbPath);
        }
        hc.setMaximumPoolSize(cfg.poolMaxSize());
        hc.setMinimumIdle(cfg.poolMinIdle());
        hc.setConnectionTimeout(cfg.connTimeoutMs());
        long leak = cfg.leakDetectMs();
        if (leak > 0) hc.setLeakDetectionThreshold(leak);
        hc.setPoolName("GratitudeRoses-DS");
        this.dataSource = new HikariDataSource(hc);
    }

    private void initSchema() {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            if ("SQLITE".equalsIgnoreCase(dialect)) {
                st.executeUpdate("PRAGMA journal_mode=WAL");
                st.executeUpdate("PRAGMA synchronous=NORMAL");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS launches (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "uuid TEXT NOT NULL, " +
                        "name TEXT, " +
                        "timestamp INTEGER NOT NULL" +
                        ")");
                try { st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_launches_uuid ON launches(uuid)"); } catch (SQLException ignored) {}
            } else {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS launches (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "name VARCHAR(64), " +
                        "timestamp BIGINT NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                try { st.executeUpdate("CREATE INDEX idx_launches_uuid ON launches (uuid)"); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("DB init error: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public long recordLaunch(UUID uuid, String name) throws SQLException {
        long now = Instant.now().toEpochMilli();
        try (Connection con = getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO launches(uuid,name,timestamp) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.setLong(3, now);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        }
        return -1L;
    }

    public long totalLaunches() throws SQLException {
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM launches"); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    public long playerLaunches(UUID uuid) throws SQLException {
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM launches WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    public Long firstLaunch(UUID uuid) throws SQLException {
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement("SELECT MIN(timestamp) FROM launches WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long v = rs.getLong(1);
                    return rs.wasNull() ? null : v;
                }
            }
        }
        return null;
    }

    public void close() {
        try { if (dataSource != null) dataSource.close(); } catch (Throwable ignored) {}
    }
}
