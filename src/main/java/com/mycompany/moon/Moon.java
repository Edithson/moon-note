/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.moon;

import java.sql.Connection;
import com.mycompany.moon.model.CategoryDAO;
import com.mycompany.moon.model.NoteDAO;
import com.mycompany.moon.view.Home;
import java.sql.ResultSet;

public class Moon {

    public static void main(String[] args) {
        /*
        Accueil accueil = new Accueil();
        accueil.setVisible(true);
        */
        //java.awt.EventQueue.invokeLater(() -> new Home().setVisible(true));
        System.out.println("Lancement du Bloc-Notes...");
        
        // 1. Tester la connexion
        try (Connection conn = DBManager.connect()) {
            if (conn != null) {
                System.out.println("Test de connexion réussi. Le fichier "+ DBManager.DB_NAME +" est prêt.");
                DBManager.createNewTables();
                
                // création d'une catégorie
                NoteDAO note = new NoteDAO();
                /*
                note.delete(4);
                */
                
                ResultSet rs = note.read();
                while(rs.next()){
                    System.out.println("id : "+rs.getString("id")+" # Titre : "+rs.getString("titre")+" # Catégorie : "+rs.getString("categorie_nom")+"\n");
                }
                
                
                System.out.println("Démarrage de l'application terminé.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
    
}
