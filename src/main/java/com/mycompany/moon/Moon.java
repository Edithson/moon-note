/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.moon;

import java.sql.Connection;
import com.mycompany.moon.model.CategoryDAO;
import com.mycompany.moon.model.NoteDAO;
import com.mycompany.moon.view.Home;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public class Moon {

    public static void main(String[] args) {
        /*
        Accueil accueil = new Accueil();
        accueil.setVisible(true);
        */
        java.awt.EventQueue.invokeLater(() -> new Home().setVisible(true));
        System.out.println("Lancement du Bloc-Notes...");
        
        // 1. Tester la connexion
        try (Connection conn = DBManager.connect()) {
            if (conn != null) {
                System.out.println("Test de connexion réussi. Le fichier "+ DBManager.DB_NAME +" est prêt.");
                DBManager.createNewTables();
                
                // création d'une catégorie
                /*
                NoteDAO note = new NoteDAO();
                List<Map<String, Object>> list_cat = note.read(6);

                for (Map<String, Object> list : list_cat) {
                    System.out.println(list.get("titre").toString());
                }
                */
                
                
                System.out.println("Démarrage de l'application terminé.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
    
}
