/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.moon.model;

import java.sql.ResultSet;
import com.mycompany.moon.DBManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryDAO {
    
    public int insert(String name) {
        String sql = "INSERT INTO categories(nom) VALUES(?)";
        String[] values = {name};
        int newId = -1;
        try {
            newId = DBManager.write(sql, values);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'insertion de la catégorie : "+e.getLocalizedMessage());
        }
        return newId;
    }
    
    public int update(int id, String name) {
        String sql = "UPDATE categories SET nom=? WHERE id=?";
        String[] values = {name, String.valueOf(id)};
        int newId = -1;
        try {
            newId = DBManager.write(sql, values);
            System.out.println("Catégotie mise à jour avec succes");
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de la catégorie : "+e.getLocalizedMessage());
        }
        return newId;
    }
    
    public int delete(int id) {
        // mettre dabord à jour les notes dans une autres catégories
        if(id == 1) {
            System.err.println("Impossible de supprimer la catégorie de base");
            return -1;
        };
        String note_sql = "UPDATE notes SET categorie_id=? WHERE categorie_id=?";
        String[] note_vl = {"1", String.valueOf(id)};
        String sql = "DELETE FROM categories WHERE id=?";
        String[] values = {String.valueOf(id)};
        int newId = -1;
        try {
            DBManager.write(note_sql, note_vl);
            newId = DBManager.write(sql, values);
            System.out.println("Catégorie supprimée avec succes!");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'insertion de la catégorie : "+e.getLocalizedMessage());
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
            return new ArrayList<>();
        }
    }
}
