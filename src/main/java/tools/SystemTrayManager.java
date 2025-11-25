package tools;

import com.mycompany.moon.view.CreateNote;
import com.mycompany.moon.view.Home;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SystemTrayManager {

    private static TrayIcon trayIcon;
    private static JDialog popupDialog;

    public static void init() {
        try {
            if (trayIcon != null) {
                return; // Évite une double installation
            }

            if (!SystemTray.isSupported()) {
                System.err.println("SystemTray non supporté.");
                return;
            }

            SystemTray tray = SystemTray.getSystemTray();

            // Icône du tray
            Image image = Toolkit.getDefaultToolkit().getImage(
                    SystemTrayManager.class.getResource("/icons/mini_icon10.png")
            );

            // ----------------------------
            // Création du PopupMenu natif AWT
            // ----------------------------
            PopupMenu popup = new PopupMenu();

            MenuItem openItem = new MenuItem("Ouvrir Moon Note");
            openItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    if (Home.instance != null) {
                        Home.instance.setVisible(true);
                        Home.instance.toFront();
                        Home.instance.requestFocus();
                    } else {
                        new Home().setVisible(true);
                    }
                });
            });
            popup.add(openItem);

            MenuItem newNoteItem = new MenuItem("Nouvelle note");
            newNoteItem.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    new CreateNote(Home.instance).setVisible(true);
                });
            });
            popup.add(newNoteItem);

            popup.addSeparator();

            MenuItem exitItem = new MenuItem("Quitter");
            exitItem.addActionListener(e -> System.exit(0));
            popup.add(exitItem);

            // ----------------------------
            // TrayIcon avec menu natif
            // ----------------------------
            trayIcon = new TrayIcon(image, "Moon Note", popup);
            trayIcon.setImageAutoSize(true);

            // Double-clic pour ouvrir l'application
            trayIcon.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    if (Home.instance != null) {
                        Home.instance.setVisible(true);
                        Home.instance.toFront();
                        Home.instance.requestFocus();
                    } else {
                        new Home().setVisible(true);
                    }
                });
            });

            tray.add(trayIcon);
            System.out.println(">>> SystemTray installé avec succès.");

        } catch (Exception e) {
            System.err.println("Erreur SystemTrayManager : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche une notification dans le system tray
     */
    public static void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, type);
        }
    }

    /**
     * Supprime l'icône du system tray
     */
    public static void remove() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }
}