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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    
    public List<Map<String, Object>> read(String search_word, int search_cat){
        String sql;
        String[] finalValues;

        // 1. Préparation de la recherche avec le wildcard SQL
        String searchPattern = "%" + search_word + "%"; 

        // 2. Définition des requêtes et des paramètres
        if (search_cat == 0) { // Pas de filtre de catégorie
            sql = "SELECT n.*, c.nom as categorie_nom FROM notes n "
                + "JOIN categories c ON n.categorie_id = c.id "
                + "WHERE n.deleted_at IS NULL AND (n.titre || n.contenu) LIKE ? "
                + "ORDER BY n.created_at DESC";
            finalValues = new String[]{searchPattern};

        } else { // Filtre par catégorie
            sql = "SELECT n.*, c.nom as categorie_nom FROM notes n "
                + "JOIN categories c ON n.categorie_id = c.id "
                + "WHERE n.deleted_at IS NULL AND (n.titre || n.contenu) LIKE ? "
                + "AND n.categorie_id = ? "
                + "ORDER BY n.created_at DESC";

            // CORRECTION: Utilisation d'un nouveau tableau pour les deux paramètres
            finalValues = new String[]{searchPattern, String.valueOf(search_cat)};
        }
        try {
            System.out.println("notes reccuperées avec succes");
            return DBManager.read(sql, finalValues);
        } catch (Exception e) {
            System.err.println("Une erreur s'est produite lors de la reccupération des notes\n"+e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, Object>> read(int id) {
        String sql = "SELECT notes.*, categories.nom as categorie_nom FROM notes, categories WHERE notes.id=? AND notes.categorie_id=categories.id";
        String[] values = { String.valueOf(id) };

        try {
            return DBManager.read(sql, values);
        } catch (Exception e) {
            System.err.println("Erreur NoteDAO.read : " + e.getMessage());
            return new ArrayList<>(); // jamais null !
        }
    }

    
}
