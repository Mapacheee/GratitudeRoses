package me.mapacheee.gratituderoses.storage;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;

/* This class if to persist launches in SQLite and query totals and first launch timestamps */

@Service
public class StorageService {
    private final Plugin plugin;
    private final String dbPath;

    @Inject
    public StorageService(Plugin plugin, me.mapacheee.gratituderoses.shared.ConfigService cfg) {
        this.plugin = plugin;
        File data = plugin.getDataFolder();
        if (!data.exists()) data.mkdirs();
        this.dbPath = new File(data, cfg.dbFile()).getAbsolutePath();
        init();
    }

    private void init() {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("PRAGMA journal_mode=WAL");
            st.executeUpdate("PRAGMA synchronous=NORMAL");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS launches (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "uuid TEXT NOT NULL, " +
                    "name TEXT, " +
                    "timestamp INTEGER NOT NULL" +
                    ")");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_launches_uuid ON launches(uuid)");
        } catch (SQLException e) {
            plugin.getLogger().severe("DB init error: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
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
}

