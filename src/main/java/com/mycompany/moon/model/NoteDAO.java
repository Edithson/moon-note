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
    
    public static int insert(int categorie, String titre, String contenu){
        String sql = "INSERT INTO notes(titre, contenu, created_at, updated_at, deleted_at, categorie_id) VALUES (?, ?, ?, ?, ?, ?)";
        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] values = {titre, contenu, currentDateTime.toString(), null, null, String.valueOf(categorie)};
        int nexID = -1;
        try {
            nexID = DBManager.write(sql, values);
            System.out.println("Création de la note réussie!");
        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de l'ajout de la note... " + e.getLocalizedMessage());
        }
        return nexID;
    }
    
    public static int update(int id_note, int id_categorie, String titre, String contenu){
        String sql = "UPDATE notes SET titre=?, contenu=?, updated_at=?, categorie_id=? WHERE id=?";
        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] values = {titre, contenu, currentDateTime.toString(), String.valueOf(id_categorie), String.valueOf(id_note)};
        int nexID = -1;
        try {
            nexID = DBManager.write(sql, values);
            System.out.println("Mise à jour de la note réussie!");
        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de la mise à jour de la note... " + e.getLocalizedMessage());
        }
        return nexID;
    }
    
    public static int delete(int id_note){
        String sql = "UPDATE notes SET deleted_at=? WHERE id=?";
        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] values = {currentDateTime.toString(), String.valueOf(id_note)};
        int nexID = -1;
        try {
            DBManager.write(sql, values);
            System.out.println("Suppression de la note réussie!");
        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de la suppression de la note... " + e.getLocalizedMessage());
        }
        return nexID;
    }
    
    public ResultSet read(){
        ResultSet rs = null;
        String sql = "SELECT notes.*, categories.nom as categorie_nom FROM notes, categories WHERE deleted_at IS NULL AND notes.categorie_id=categories.id ORDER BY created_at DESC";
        try {
            rs = DBManager.read(sql);
            System.err.println("notes reccuperées avec succes");
        } catch (Exception e) {
            System.err.println("Une erreur s'est produite lors de la reccupération des notes\n"+e.getLocalizedMessage());
        } finally {
            return rs;
        }
    }
    
}
