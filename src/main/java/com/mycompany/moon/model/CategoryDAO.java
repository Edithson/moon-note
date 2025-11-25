/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.moon.model;

import com.mycompany.moon.DBManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.DialogMsg;
import tools.UniqueID;

public class CategoryDAO {
    
    public int insert(String name) {
        String sql = "INSERT INTO categories(id, nom) VALUES(?, ?)";
        String id = UniqueID.generateUniqueCode();
        String[] values = {id, name};
        int newId = -1;
        try {
            newId = DBManager.write(sql, values);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'insertion de la catégorie : "+e.getLocalizedMessage());
            DialogMsg.errorMsg("Erreur lors de l'insertion de la catégorie : "+e.getLocalizedMessage());
        }
        return newId;
    }
    
    public int update(String id, String name) {
        if(id == "1") {
            System.err.println("Impossible de modifier la catégorie de base");
            return -1;
        };
        String sql = "UPDATE categories SET nom=? WHERE id=?";
        String[] values = {name, id};
        int newId = -1;
        try {
            newId = DBManager.write(sql, values);
            System.out.println("Catégotie mise à jour avec succes");
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de la catégorie : "+e.getLocalizedMessage());
            DialogMsg.errorMsg("Erreur lors de la mise à jour de la catégorie : "+e.getLocalizedMessage());
        }
        return newId;
    }
    
    public int delete(String id) {
        // mettre dabord à jour les notes dans une autres catégories
        if(id == "1") {
            System.err.println("Impossible de supprimer la catégorie de base");
            return -1;
        };
        String note_sql = "UPDATE notes SET categorie_id=? WHERE categorie_id=?";
        String[] note_vl = {"1", id};
        String sql = "DELETE FROM categories WHERE id=?";
        String[] values = {String.valueOf(id)};
        int newId = -1;
        try {
            DBManager.write(note_sql, note_vl);
            newId = DBManager.write(sql, values);
            System.out.println("Catégorie supprimée avec succes!");
            DialogMsg.successMsg("Catégorie supprimée avec succes!");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'insertion de la catégorie : "+e.getLocalizedMessage());
            DialogMsg.errorMsg("Erreur lors de l'insertion de la catégorie : "+e.getLocalizedMessage());
        }
        return newId;
    }
    
    public List<Map<String, Object>> read(String nom_cat){
        String searchPattern = "%" + nom_cat + "%";
        String[] finalValues = new String[]{searchPattern};
        String sql = "SELECT * FROM categories WHERE nom LIKE ? ORDER BY id DESC";
        try {
            System.err.println("Catégories reccuperées avec succes");
            return DBManager.read(sql, finalValues);
        } catch (Exception e) {
            System.err.println("Une erreur s'est produite lors de la reccupération des catégories\n"+e.getLocalizedMessage());
            DialogMsg.errorMsg("Une erreur s'est produite lors de la reccupération des catégories\n"+e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, Object>> read(String nom, String id_cat){
        String[] values = {id_cat};
        String sql = "SELECT * FROM categories WHERE id=?";
        try {
            System.err.println("Catégories reccuperées avec succes");
            return DBManager.read(sql, values);
        } catch (Exception e) {
            System.err.println("Une erreur s'est produite lors de la reccupération de la catégorie\n"+e.getLocalizedMessage());
            DialogMsg.errorMsg("Une erreur s'est produite lors de la reccupération de la catégorie\n"+e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lit l'ID d'une catégorie par son nom exact. Utile pour vérifier les doublons de nom.
     * @param name Le nom exact de la catégorie.
     * @return L'ID de la catégorie existante (String), ou null si elle n'existe pas.
     */
    public String getIdByName(String name) {
        String sql = "SELECT id FROM categories WHERE nom = ?";
        String[] values = {name};
        try {
            List<Map<String, Object>> result = DBManager.read(sql, values);
            if (result != null && !result.isEmpty()) {
                // L'ID est stocké en TEXT, on le récupère.
                Object idObj = result.get(0).get("id");
                return (idObj != null) ? idObj.toString() : null;
            }
        } catch (Exception e) {
            System.err.println("Erreur CategoryDAO.getIdByName : " + e.getLocalizedMessage());
        }
        return null;
    }

    /**
    * Récupère les données d'une catégorie (active ou supprimée) par son ID.
    * @param id L'ID unique de la catégorie.
    * @return La Map de données de la catégorie, ou null si non trouvée.
    */
   public Map<String, Object> getCategoryById(String id) {
       String sql = "SELECT id, nom, deleted_at FROM categories WHERE id = ?";
       String[] values = {id};
       try {
           List<Map<String, Object>> result = DBManager.read(sql, values);
           if (result != null && !result.isEmpty()) {
               return result.get(0);
           }
       } catch (Exception e) {
           System.err.println("Erreur CategoryDAO.getCategoryById : " + e.getLocalizedMessage());
       }
       return null;
   }

   /**
    * Insère une catégorie ou met à jour/restaure une catégorie existante.
    * @param id L'ID unique de la catégorie.
    * @param name Le nom de la catégorie.
    * @return L'ID de la catégorie traitée, ou null si erreur.
    */
   public String upsertForImport(String id, String name) {
       Map<String, Object> existingCat = getCategoryById(id);

       if (existingCat != null) {
           // L'ID existe déjà (doublon d'ID)
           String deletedAt = (String) existingCat.get("deleted_at");

           if (deletedAt != null && !deletedAt.isEmpty()) {
               // Le doublon est soft-deleted -> RESTAURER/METTRE À JOUR
               String sql = "UPDATE categories SET nom=?, updated_at=datetime('now'), deleted_at=NULL WHERE id=?";
               String[] values = {name, id};
               try {
                   DBManager.write(sql, values);
                   return id; // ID restauré
               } catch (Exception e) {
                   System.err.println("Erreur lors de la restauration de la catégorie (import) : " + e.getLocalizedMessage());
                   return null;
               }
           } else {
               // Le doublon est ACTIF -> IGNORER L'UPDATE (utiliser l'existant)
               return id;
           }
       } else {
           // L'ID n'existe pas -> INSERTION
           String sql = "INSERT INTO categories(id, nom, created_at, updated_at, deleted_at) VALUES(?, ?, datetime('now'), NULL, NULL)";
           String[] values = {id, name};
           try {
               DBManager.write(sql, values);
               return id; // Nouvel ID inséré
           } catch (Exception e) {
               System.err.println("Erreur lors de l'insertion de la catégorie (import) : " + e.getLocalizedMessage());
               return null;
           }
       }
   }

}
