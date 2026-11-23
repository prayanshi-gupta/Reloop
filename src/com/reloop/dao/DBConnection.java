package com.reloop.dao;

import com.reloop.exception.DatabaseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Single place that knows HOW to connect to MySQL.
 * Every DAO calls DBConnection.getConnection() and never touches
 * DriverManager directly.
 *
 * Concepts: static members, private constructor, File handling
 * (FileInputStream + Properties), JDBC DriverManager, try-with-resources,
 * exception wrapping.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "config/db.properties";

    // static: shared by the whole application, loaded only once
    private static String url;
    private static String user;
    private static String password;
    private static boolean loaded = false;

    // private constructor: utility class, nobody should do "new DBConnection()"
    private DBConnection() { }

    /** Reads db.properties once. synchronized so two threads can't load it twice. */
    private static synchronized void loadConfig() throws DatabaseException {
        if (loaded) {
            return;
        }
        Properties props = new Properties();
        // try-with-resources closes the file automatically, even if an error occurs
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException e) {
            throw new DatabaseException(
                "Cannot read " + CONFIG_FILE + ". Copy db.properties.example to db.properties first.", e);
        }
        url = props.getProperty("db.url");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");
        if (url == null || user == null || password == null) {
            throw new DatabaseException("db.properties must contain db.url, db.user and db.password");
        }
        loaded = true;
    }

    /** Opens a NEW connection. The caller must close it (use try-with-resources). */
    public static Connection getConnection() throws DatabaseException {
        loadConfig();
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException("Could not connect to MySQL: " + e.getMessage(), e);
        }
    }
}
