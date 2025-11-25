/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.moon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                + "id TEXT PRIMARY KEY,"
                + "nom TEXT NOT NULL UNIQUE"
                + ");";

        String sqlNotes = "CREATE TABLE IF NOT EXISTS notes ("
                + "id TEXT PRIMARY KEY,"
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
            
            ensureDefaultCategoryExists();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la création des tables : " + e.getMessage());
        }
    }
    
    /**
     * S'assure que la catégorie de base "Non classé" avec l'ID 1 existe.
     * Cette méthode doit être appelée après la création des tables.
     */
    public static void ensureDefaultCategoryExists() {
        String name = "Non classé";
        
        // Requête pour vérifier si la catégorie existe déjà (soit par ID 1, soit par NOM)
        String checkSql = "SELECT COUNT(*) FROM categories WHERE id = 1 OR nom = ?";
        
        // Requête d'insertion forcée pour l'ID 1 si elle n'existe pas
        // On utilise INSERT OR IGNORE pour éviter les erreurs si la ligne existe mais a un autre nom.
        String insertSql = "INSERT OR IGNORE INTO categories (id, nom) VALUES (1, ?)";
        
        // Utilisation d'un compteur pour voir si l'insertion est nécessaire
        int count = 0;
        
        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            // 1. VÉRIFICATION
            checkStmt.setString(1, name);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }

            // 2. INSERTION SI NÉCESSAIRE
            if (count == 0) {
                // Si la catégorie n'existe pas, on tente de l'insérer avec l'ID 1
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, name);
                    int affectedRows = insertStmt.executeUpdate();
                    
                    if (affectedRows > 0) {
                        System.out.println("Catégorie par défaut 'Non classé' (ID 1) créée.");
                    } else {
                        // Cela arrive si l'ID 1 est déjà pris par autre chose (conflit d'ID)
                        System.err.println("Avertissement: L'ID 1 de la catégorie est déjà utilisé, 'Non classé' n'a pas pu être insérée comme ID 1.");
                    }
                }
            } else {
                System.out.println("Catégorie par défaut 'Non classé' (ID 1) déjà présente.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification/création de la catégorie par défaut : " + e.getMessage());
        }
    }
    
    public static int write(String sql, String[] values){
        int newId = -1;

        // Le try-with-resources assure la fermeture de la connexion et du statement
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1. Configurer les paramètres de la requête
            for (int i = 1; i <= values.length; i++) {
                pstmt.setString(i, values[i-1]);
            }

            // 2. Exécuter l'insertion
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // 3. Récupérer l'ID auto-généré par SQLite
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newId = rs.getInt(1);
                        System.out.println("Elément inséré avec succès. ID: " + newId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de l'élément : " + e.getMessage());
        }
        return newId;
    }
    
    public static List<Map<String, Object>> read(String sql) {
        List<Map<String, Object>> elements = new ArrayList<>();

        try (Connection cnx = DBManager.connect();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    String column = meta.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(column, value);
                }

                elements.add(row);
            }

            System.out.println("Requête exécutée avec succès");

        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
        }

        return elements;
    }
    
    public static List<Map<String, Object>> read(String sql, String[] values) {
        List<Map<String, Object>> elements = new ArrayList<>();

        try (Connection cnx = DBManager.connect();
             PreparedStatement pstmt = cnx.prepareStatement(sql)) {

            // Injecte les paramètres
            for (int i = 0; i < values.length; i++) {
                pstmt.setString(i + 1, values[i]);
            }

            // Exécute la requête
            ResultSet rs = pstmt.executeQuery();

            // Récupère les meta après executeQuery
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // Parcours des résultats
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }

                elements.add(row);
            }

            System.out.println("Requête exécutée avec succès");

        } catch (SQLException e) {
            System.err.println("Erreur dans read: " + e.getMessage());
        }

        return elements;
    }

}
