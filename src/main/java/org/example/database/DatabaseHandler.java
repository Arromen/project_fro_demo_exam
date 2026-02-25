package org.example.database;

import java.sql.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:h2:file:./database/shoe_store;MODE=MySQL";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    private static Connection connection;
    private static boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            if (!initialized) {
                initializeDatabase();
                initialized = true;
            }
        }
        return connection;
    }

    private static void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            InputStream is = DatabaseHandler.class.getResourceAsStream("/init_db.sql");
            if (is != null) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                String script = sb.toString();
                stmt.execute(script);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
