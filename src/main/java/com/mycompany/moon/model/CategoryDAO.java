/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.moon.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.mycompany.moon.DBManager;

public class CategoryDAO {
    
    public int insert(String name) {
        String sql = "INSERT INTO categories(nom) VALUES(?)";
        int newId = -1;

        // Le try-with-resources assure la fermeture de la connexion et du statement
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1. Configurer les paramètres de la requête
            pstmt.setString(1, name);

            // 2. Exécuter l'insertion
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // 3. Récupérer l'ID auto-généré par SQLite
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newId = rs.getInt(1);
                        System.out.println("Catégorie insérée avec succès. ID: " + newId);
                    }
                }
            }
        } catch (SQLException e) {
            // Gérer l'erreur, notamment si le nom de catégorie existe déjà (UNIQUE constraint)
            System.err.println("Erreur lors de l'insertion de la catégorie '" + name + "': " + e.getMessage());
        }
        return newId;
    }
}
