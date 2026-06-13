package it.kimia.repository;

import it.kimia.db.Database;
import it.kimia.model.AppUser;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class UserRepository {
    public Optional<AppUser> findByUsername(String username) throws SQLException {
        String sql = "SELECT username, password_hash, display_name FROM app_users WHERE lower(username)=lower(?) LIMIT 1";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                AppUser user = new AppUser();
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setDisplayName(rs.getString("display_name"));
                return Optional.of(user);
            }
        }
    }
}
