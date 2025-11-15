/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.moon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
    // Nom du fichier de la base de données
    public static final String DB_NAME = "notepad.db";
    
    // URL de connexion JDBC pour SQLite. 
    // Si le fichier n'existe pas, SQLite le créera automatiquement.
    private static final String URL = "jdbc:sqlite:" + DB_NAME;

    /**
     * Établit et retourne une connexion à la base de données SQLite.
     * @return L'objet Connection ou null en cas d'erreur.
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            // L'API JDBC trouve le pilote xerial grâce au DriverManager
            conn = DriverManager.getConnection(URL);
            System.out.println("Connexion à la base de données SQLite établie.");
            return conn;
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la DB : " + e.getMessage());
            return null;
        }
    }
    
    // création des tables
    public static void createNewTables() {
        // Requêtes SQL pour la création des tables
        String sqlCategories = "CREATE TABLE IF NOT EXISTS categories ("
                + "id INTEGER PRIMARY KEY,"
                + "nom TEXT NOT NULL UNIQUE"
                + ");";

        String sqlNotes = "CREATE TABLE IF NOT EXISTS notes ("
                + "id INTEGER PRIMARY KEY,"
                + "titre TEXT NOT NULL,"
                + "contenu TEXT,"
                + "created_at TEXT NOT NULL," // En SQLite, on utilise TEXT pour les dates
                + "updated_at TEXT,"
                + "deleted_at TEXT,"
                + "categorie_id INTEGER,"
                + "FOREIGN KEY (categorie_id) REFERENCES categories(id) ON DELETE SET NULL"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            // Exécuter la requête de création de la table categories
            stmt.execute(sqlCategories);
            System.out.println("Table 'categories' vérifiée/créée.");
            
            // Exécuter la requête de création de la table notes
            stmt.execute(sqlNotes);
            System.out.println("Table 'notes' vérifiée/créée.");

        } catch (SQLException e) {
            System.err.println("Erreur lors de la création des tables : " + e.getMessage());
        }
    }
}
