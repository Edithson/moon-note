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
import java.time.LocalDateTime;

public class NoteDAO {
    
    public int insert(int categorie, String titre, String contenu){
        String sql = "INSERT INTO notes(titre, contenu, created_at, updated_at, deleted_at, categorie_id) VALUES (?, ?, ?, ?, ?, ?)";
        LocalDateTime currentDateTime = LocalDateTime.now();
        int nexID = -1;
        try {
            Connection cnx = DBManager.connect();
            PreparedStatement pstmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, titre);
            pstmt.setString(2, contenu);
            pstmt.setString(3, currentDateTime.toString());
            pstmt.setString(4, null);
            pstmt.setString(5, null);
            pstmt.setInt(6, categorie);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try {
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        nexID = rs.getInt(1);
                        System.out.println("note crée avec succes !");
                    }
                } catch (Exception e) {
                    System.err.println("Une erreur est survenue... " + e.getLocalizedMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Une erreur est survenue... " + e.getLocalizedMessage());
        }
        return nexID;
    }
    
}
