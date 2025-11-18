/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.moon;

import com.mycompany.moon.view.CreateNote;
import java.sql.Connection;
import com.mycompany.moon.view.Home;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import tools.ExportManager;
import tools.SystemTrayManager;

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
            // Initialise le système de notifications (tray)
            SystemTrayManager.init();
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }

    }
    
}
