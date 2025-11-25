/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.moon.model;

import com.mycompany.moon.DBManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.DialogMsg;
import tools.NotificationManager;
import tools.UniqueID;

public class NoteDAO {
    
    public static int insert(String categorie, String titre, String contenu){
        String sql = "INSERT INTO notes(id, titre, contenu, created_at, updated_at, deleted_at, categorie_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime currentDateTime = LocalDateTime.now();
        String id = UniqueID.generateUniqueCode();
        String[] values = {id, titre, contenu, currentDateTime.toString(), null, null, categorie};
        int nexID = -1;
        try {
            nexID = DBManager.write(sql, values);
            System.out.println("Création de la note réussie!");
            NotificationManager.notify("Moon Note par GZ", "Votre note a bien été crée !");
            DialogMsg.successMsg("Votre note a bien été crée !");
        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de l'ajout de la note... " + e.getLocalizedMessage());
            DialogMsg.errorMsg("Une erreur est survenue lors de l'ajout de la note... " + e.getLocalizedMessage());
        }
        return nexID;
    }
    
    public static int update(String id_note, String id_categorie, String titre, String contenu){
        String sql = "UPDATE notes SET titre=?, contenu=?, updated_at=?, categorie_id=? WHERE id=?";
        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] values = {titre, contenu, currentDateTime.toString(), id_categorie, id_note};
        int nexID = -1;
        try {
            nexID = DBManager.write(sql, values);
            System.out.println("Mise à jour de la note réussie!");
            DialogMsg.successMsg("Mise à jour de la note réussie!");
        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de la mise à jour de la note... " + e.getLocalizedMessage());
            DialogMsg.errorMsg("Une erreur est survenue lors de la mise à jour de la note... " + e.getLocalizedMessage());
        }
        return nexID;
    }
    
    public static int delete(String id_note){
        String sql = "UPDATE notes SET deleted_at=? WHERE id=?";
        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] values = {currentDateTime.toString(), id_note};
        int nexID = -1;
        try {
            DBManager.write(sql, values);
            System.out.println("Suppression de la note réussie!");
            DialogMsg.successMsg("Suppression de la note réussie!");
        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de la suppression de la note... " + e.getLocalizedMessage());
            DialogMsg.errorMsg("Une erreur est survenue lors de la suppression de la note... " + e.getLocalizedMessage());
        }
        return nexID;
    }
    
    public List<Map<String, Object>> read(String search_word, String search_cat){
        String sql;
        String[] finalValues;

        // 1. Préparation de la recherche avec le wildcard SQL
        String searchPattern = "%" + search_word + "%"; 

        // 2. Définition des requêtes et des paramètres
        if (search_cat == "0") { // Pas de filtre de catégorie
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
            finalValues = new String[]{searchPattern, search_cat};
        }
        try {
            System.out.println("notes reccuperées avec succes");
            return DBManager.read(sql, finalValues);
        } catch (Exception e) {
            System.err.println("Une erreur s'est produite lors de la reccupération des notes\n"+e.getLocalizedMessage());
            DialogMsg.errorMsg("Une erreur s'est produite lors de la reccupération des notes\n"+e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, Object>> read(String id) {
        String sql = "SELECT notes.*, categories.nom as categorie_nom FROM notes, categories WHERE notes.id=? AND notes.categorie_id=categories.id";
        String[] values = { id };

        try {
            return DBManager.read(sql, values);
        } catch (Exception e) {
            System.err.println("Erreur NoteDAO.read : " + e.getMessage());
            DialogMsg.errorMsg("Erreur NoteDAO.read : " + e.getMessage());
            return new ArrayList<>(); // jamais null !
        }
    }

    /**
     * Insère une note avec un ID et des timestamps pré-générés (pour l'importation).
     * Utilise INSERT OR IGNORE pour éviter les conflits d'ID.
     */
    public static int insertForImport(String id_note, String id_categorie, String titre, String contenu, String created_at, String updated_at){
        // INSERT OR IGNORE : si l'ID de la note existe déjà (conflit de clé primaire), l'insertion est ignorée.
        String sql = "INSERT OR IGNORE INTO notes(id, titre, contenu, created_at, updated_at, deleted_at, categorie_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Si updated_at est vide ou null, nous le mappons à null dans la DB.
        String finalUpdatedAt = (updated_at == null || updated_at.isEmpty()) ? null : updated_at;

        // deleted_at est toujours null lors de l'import d'une note active
        String[] values = {id_note, titre, contenu, created_at, finalUpdatedAt, null, id_categorie};
        try {
            DBManager.write(sql, values); 
            return 1; // Succès (insérée ou ignorée)
        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de l'ajout de la note (import)... " + e.getLocalizedMessage());
            DialogMsg.errorMsg("Une erreur est survenue lors de l'ajout de la note (import)... " + e.getLocalizedMessage());
            return -1;
        }
    }

    /**
     * Insère une note ou met à jour/restaure une note existante si elle était soft-deleted.
     * @return 1 si la note a été insérée ou restaurée, 0 si ignorée (doublon actif), -1 si erreur.
     */
    public static int upsertNoteForImport(String id_note, String id_categorie, String titre, String contenu, String created_at, String updated_at){

        // 1. VÉRIFICATION DE L'EXISTENCE ET DU STATUT
        String selectSql = "SELECT deleted_at FROM notes WHERE id = ?";
        String[] selectValues = {id_note};
        String existingDeletedAt = null;
        try {
            List<Map<String, Object>> result = DBManager.read(selectSql, selectValues);
            if (result != null && !result.isEmpty()) {
                existingDeletedAt = (String) result.get(0).get("deleted_at");
            }
        } catch (Exception e) {
            System.err.println("Erreur de sélection de la note lors de l'import : " + e.getLocalizedMessage());
            return -1;
        }

        String finalUpdatedAt = (updated_at == null || updated_at.isEmpty()) ? null : updated_at;

        if (existingDeletedAt != null && !existingDeletedAt.isEmpty()) {
            // 2. RESTAURATION/MISE À JOUR (deleted_at != null)
            String updateSql = "UPDATE notes SET titre=?, contenu=?, categorie_id=?, created_at=?, updated_at=?, deleted_at=NULL WHERE id=?";
            String[] updateValues = {titre, contenu, id_categorie, created_at, finalUpdatedAt, id_note};
            try {
                DBManager.write(updateSql, updateValues);
                return 2; // Code 2 pour "Restaurée" pour le rapport
            } catch (Exception e) {
                System.err.println("Erreur lors de la restauration de la note (import) : " + e.getLocalizedMessage());
                return -1;
            }

        } else if (existingDeletedAt == null && existingDeletedAt != null) {
            // 3. IGNORER (deleted_at == null) : ID déjà actif
            return 0; 

        } else {
            // 4. INSERTION : ID non trouvé
            String insertSql = "INSERT INTO notes(id, titre, contenu, created_at, updated_at, deleted_at, categorie_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String[] insertValues = {id_note, titre, contenu, created_at, finalUpdatedAt, null, id_categorie};
            try {
                DBManager.write(insertSql, insertValues); 
                return 1; // Insérée
            } catch (Exception e) {
                System.err.println("Une erreur est survenue lors de l'ajout de la note (import)... " + e.getLocalizedMessage());
                return -1;
            }
        }
    }

    
}
