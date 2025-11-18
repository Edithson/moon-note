/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.moon;

import java.sql.Connection;
import com.mycompany.moon.view.Home;
import tools.ExportManager;

public class Moon {

    public static void main(String[] args) {
        // System.out.println("Exportation des données...");
        // ExportManager.exportDatabase();

        java.awt.EventQueue.invokeLater(() -> new Home().setVisible(true));
        System.out.println("Lancement du Bloc-Notes...");
        
        // 1. Tester la connexion
        try (Connection conn = DBManager.connect()) {
            if (conn != null) {
                System.out.println("Test de connexion réussi. Le fichier "+ DBManager.DB_NAME +" est prêt.");
                DBManager.createNewTables();

                System.out.println("Démarrage de l'application terminé.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
    
}
