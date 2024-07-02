package com.urlShortner.service;

import com.urlShortner.dbConn.DatabaseConnection;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;

@Service
public class URLShortenerService {
    private static final String PREFIX = "http://localhost:8080/";

    public String shortenURL(String longUrl) throws SQLException, NoSuchAlgorithmException {
        String shortUrl = generateShortURL(longUrl);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO urls (short_url, long_url) VALUES (?, ?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, shortUrl);
                stmt.setString(2, longUrl);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return PREFIX + shortUrl;
                }
            }
        }
        return null;
    }

    private String generateShortURL(String longUrl) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(longUrl.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(Integer.toHexString(0xFF & b));
        }
        return hexString.toString().substring(0, 10); // taking first 10 characters
    }

    public boolean updateShortURL(String shortUrl, String newLongUrl) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE urls SET long_url = ?, updated_at = NOW() WHERE short_url = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, newLongUrl);
                stmt.setString(2, shortUrl.replace(PREFIX, ""));
                int rowsUpdated = stmt.executeUpdate();
                return rowsUpdated > 0;
            }
        }
    }

    public String getLongURL(String shortUrl) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT long_url FROM urls WHERE short_url = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, shortUrl.replace(PREFIX, ""));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("long_url");
                }
            }
        }
        return null;
    }

    public boolean updateExpiry(String shortUrl, int daysToAdd) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE urls SET expiry = expiry + INTERVAL '? days' WHERE short_url = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, daysToAdd);
                stmt.setString(2, shortUrl.replace(PREFIX, ""));
                int rowsUpdated = stmt.executeUpdate();
                return rowsUpdated > 0;
            }
        }
    }
}
